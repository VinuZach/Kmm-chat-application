//
//  AudioPlayerManager.swift
//  iosApp
//
//  Created by Vinu on 03/09/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import AVFoundation
import Combine

// MARK: - Audio Player Manager
class AudioPlayerManager: NSObject, ObservableObject {
    @Published var isPlaying = false
    @Published var isLoading = false
    @Published var currentTime: Double = 0
    @Published var duration: Double = 0
    @Published var progress: Double = 0
    @Published var statusMessage = "Ready to play"
    @Published var hasError = false
    
    var player: AVPlayer?
    private var playerItem: AVPlayerItem?
    private var timeObserver: Any?
    private var cancellables = Set<AnyCancellable>()
    
    override init() {
        super.init()
        setupAudioSession()
    }
    
    private func setupAudioSession() {
        do {
            let audioSession = AVAudioSession.sharedInstance()
            try audioSession.setCategory(.playback, mode: .default, options: [.allowAirPlay, .allowBluetooth])
            try audioSession.setActive(true)
            print("✅ Audio session setup successful")
        } catch {
            print("❌ Failed to setup audio session: \(error)")
            statusMessage = "Audio session setup failed"
            hasError = true
        }
    }
    
    func playFromURL(_ urlString: String) {
        print("🎵 Attempting to play URL: \(urlString)")
        
        guard let url = URL(string: urlString) else {
            print("❌ Invalid URL: \(urlString)")
            statusMessage = "Invalid URL"
            hasError = true
            return
        }
        
        // First, test if the URL is reachable
        testURLReachability(url: url) { [weak self] isReachable in
            DispatchQueue.main.async {
                if isReachable {
                    self?.setupPlayerWithURL(url)
                } else {
                    print("❌ URL is not reachable")
                    self?.statusMessage = "URL not reachable"
                    self?.hasError = true
                    self?.isLoading = false
                }
            }
        }
        
        isLoading = true
        statusMessage = "Loading..."
        hasError = false
    }
    
    private func testURLReachability(url: URL, completion: @escaping (Bool) -> Void) {
        var request = URLRequest(url: url)
        request.httpMethod = "HEAD"
        request.timeoutInterval = 10.0
        
        URLSession.shared.dataTask(with: request) { _, response, error in
            if let error = error {
                print("❌ URL reachability test failed: \(error.localizedDescription)")
                completion(false)
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                print("📡 HTTP Status: \(httpResponse.statusCode)")
                print("📋 Content-Type: \(httpResponse.allHeaderFields["Content-Type"] ?? "Unknown")")
                print("📏 Content-Length: \(httpResponse.allHeaderFields["Content-Length"] ?? "Unknown")")
                
                let isSuccess = (200...299).contains(httpResponse.statusCode)
                completion(isSuccess)
            } else {
                completion(false)
            }
        }.resume()
    }
    
    private func setupPlayerWithURL(_ url: URL) {
        cleanupPlayer()
        
        print("🔧 Setting up player with URL: \(url)")
        
        // Create player item
        playerItem = AVPlayerItem(url: url)
        
        // Add observers before creating player
        addObserversToPlayerItem()
        
        // Create player
        player = AVPlayer(playerItem: playerItem)
        
        // Add time observer
        addPeriodicTimeObserver()
        
        // Monitor playback completion
        NotificationCenter.default.publisher(for: .AVPlayerItemDidPlayToEndTime, object: playerItem)
            .sink { [weak self] _ in
                print("🏁 Playback completed")
                self?.playerDidFinishPlaying()
            }
            .store(in: &cancellables)
        
        // Monitor playback stalling
        NotificationCenter.default.publisher(for: .AVPlayerItemPlaybackStalled, object: playerItem)
            .sink { [weak self] _ in
                print("⏸️ Playback stalled")
                self?.statusMessage = "Playback stalled"
            }
            .store(in: &cancellables)
        
        // Start playing after a short delay
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self] in
            self?.startPlayback()
        }
    }
    
    private func addObserversToPlayerItem() {
        guard let playerItem = playerItem else { return }
        
        // Add KVO observers
        playerItem.addObserver(self, forKeyPath: "status", options: [.new, .initial], context: nil)
        playerItem.addObserver(self, forKeyPath: "duration", options: [.new, .initial], context: nil)
        playerItem.addObserver(self, forKeyPath: "loadedTimeRanges", options: [.new, .initial], context: nil)
        playerItem.addObserver(self, forKeyPath: "playbackBufferEmpty", options: [.new, .initial], context: nil)
        playerItem.addObserver(self, forKeyPath: "playbackLikelyToKeepUp", options: [.new, .initial], context: nil)
        
        print("🔍 Added observers to player item")
    }
    
    private func startPlayback() {
        guard let player = player else {
            print("❌ No player available")
            return
        }
        
        print("▶️ Starting playback...")
        player.play()
        isPlaying = true
        statusMessage = "Starting playback..."
    }
    
    func togglePlayback() {
        guard let player = player else {
            print("❌ No player available for toggle")
            return
        }
        
        if isPlaying {
            print("⏸️ Pausing playback")
            player.pause()
            isPlaying = false
            statusMessage = "Paused"
        } else {
            print("▶️ Resuming playback")
            player.play()
            isPlaying = true
            statusMessage = "Playing"
        }
    }
    
    func stop() {
        guard let player = player else { return }
        
        print("⏹️ Stopping playback")
        player.pause()
        player.seek(to: .zero)
        isPlaying = false
        currentTime = 0
        progress = 0
        statusMessage = "Stopped"
    }
    
    func seek(to time: Double) {
        guard let player = player else { return }
        
        let cmTime = CMTime(seconds: time, preferredTimescale: CMTimeScale(NSEC_PER_SEC))
        print("⏭️ Seeking to: \(time) seconds")
        player.seek(to: cmTime)
    }
    
    private func addPeriodicTimeObserver() {
        let timeScale = CMTimeScale(NSEC_PER_SEC)
        let time = CMTime(seconds: 0.5, preferredTimescale: timeScale) // Reduced frequency
        
        timeObserver = player?.addPeriodicTimeObserver(forInterval: time, queue: .main) { [weak self] time in
            self?.updateProgress()
        }
        
        print("⏱️ Added periodic time observer")
    }
    
    private func updateProgress() {
        guard let player = player,
              let playerItem = player.currentItem else { return }
        
        let currentTime = player.currentTime()
        let duration = playerItem.duration
        
        self.currentTime = currentTime.seconds
        
        if duration.isValid && !duration.isIndefinite {
            self.duration = duration.seconds
            self.progress = duration.seconds > 0 ? currentTime.seconds / duration.seconds : 0
            
            let current = Int(currentTime.seconds)
            let total = Int(duration.seconds)
            if isPlaying {
                statusMessage = "Playing: \(formatTime(current)) / \(formatTime(total))"
            }
        }
    }
    
    private func formatTime(_ seconds: Int) -> String {
        let minutes = seconds / 60
        let remainingSeconds = seconds % 60
        return String(format: "%d:%02d", minutes, remainingSeconds)
    }
    
    private func playerDidFinishPlaying() {
        isPlaying = false
        statusMessage = "Playback completed"
        progress = 1.0
    }
    
    private func cleanupPlayer() {
        print("🧹 Cleaning up player")
        
        if let timeObserver = timeObserver {
            player?.removeTimeObserver(timeObserver)
            self.timeObserver = nil
        }
        
        // Remove observers safely
        if let playerItem = playerItem {
            playerItem.removeObserver(self, forKeyPath: "status")
            playerItem.removeObserver(self, forKeyPath: "duration")
            playerItem.removeObserver(self, forKeyPath: "loadedTimeRanges")
            playerItem.removeObserver(self, forKeyPath: "playbackBufferEmpty")
            playerItem.removeObserver(self, forKeyPath: "playbackLikelyToKeepUp")
        }
        
        player?.pause()
        player = nil
        playerItem = nil
        cancellables.removeAll()
        
        // Reset state
        isPlaying = false
        isLoading = false
        currentTime = 0
        duration = 0
        progress = 0
    }
    
    override func observeValue(forKeyPath keyPath: String?,
                              of object: Any?,
                              change: [NSKeyValueChangeKey : Any]?,
                              context: UnsafeMutableRawPointer?) {
        
        DispatchQueue.main.async {
            guard let keyPath = keyPath else { return }
            
            print("🔍 KVO notification for: \(keyPath)")
            
            switch keyPath {
            case "status":
                self.handleStatusChange(object: object)
            case "duration":
                self.handleDurationChange(object: object)
            case "loadedTimeRanges":
                self.handleLoadedTimeRangesChange(object: object)
            case "playbackBufferEmpty":
                if let playerItem = object as? AVPlayerItem, playerItem.isPlaybackBufferEmpty {
                    print("📦 Playback buffer is empty")
                }
            case "playbackLikelyToKeepUp":
                if let playerItem = object as? AVPlayerItem, playerItem.isPlaybackLikelyToKeepUp {
                    print("📦 Playback likely to keep up")
                }
            default:
                break
            }
        }
    }
    
    private func handleStatusChange(object: Any?) {
        guard let playerItem = object as? AVPlayerItem else { return }
        
        switch playerItem.status {
        case .readyToPlay:
            print("✅ Player item ready to play")
            self.isLoading = false
            self.hasError = false
            if let asset = playerItem.asset as? AVURLAsset {
                print("🎵 Asset URL: \(asset.url)")
                print("📋 Asset tracks: \(asset.tracks.count)")
                for track in asset.tracks {
                    print("   Track: \(track.mediaType.rawValue)")
                }
            }
            
        case .failed:
            print("❌ Player item failed")
            self.isLoading = false
            self.isPlaying = false
            self.hasError = true
            
            if let error = playerItem.error {
                print("❌ Error details: \(error.localizedDescription)")
                if let nsError = error as NSError? {
                    print("❌ Error code: \(nsError.code)")
                    print("❌ Error domain: \(nsError.domain)")
                    print("❌ Error userInfo: \(nsError.userInfo)")
                }
                self.statusMessage = "Error: \(error.localizedDescription)"
            } else {
                self.statusMessage = "Unknown playback error"
            }
            
        case .unknown:
            print("❓ Player item status unknown")
            self.statusMessage = "Loading..."
            
        @unknown default:
            print("❓ Unknown player item status")
        }
    }
    
    private func handleDurationChange(object: Any?) {
        guard let playerItem = object as? AVPlayerItem else { return }
        
        let duration = playerItem.duration
        if duration.isValid && !duration.isIndefinite {
            self.duration = duration.seconds
            print("⏱️ Duration: \(duration.seconds) seconds")
        } else {
            print("⏱️ Duration not available or indefinite")
        }
    }
    
    private func handleLoadedTimeRangesChange(object: Any?) {
        guard let playerItem = object as? AVPlayerItem else { return }
        
        let loadedTimeRanges = playerItem.loadedTimeRanges
        if !loadedTimeRanges.isEmpty {
            let timeRange = loadedTimeRanges[0].timeRangeValue
            let loadedDuration = timeRange.start + timeRange.duration
            print("📦 Loaded: \(loadedDuration.seconds) seconds")
        }
    }
    
    deinit {
        cleanupPlayer()
        print("♻️ AudioPlayerManager deinitialized")
    }
}

// MARK: - SwiftUI Views
struct AudioPlayerView: View {
    @StateObject private var audioManager = AudioPlayerManager()
    @State private var audioURL = "http://192.168.1.40:8000/media/documents/audio_record_1756904418365.mp4"
    @State private var showingLogs = false
    @State private var logs: [String] = []
    
    var body: some View {
        VStack(spacing: 20) {
            Text("3GP Audio Player (Debug)")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            // URL Input
            VStack(alignment: .leading, spacing: 8) {
                Text("Audio URL:")
                    .font(.headline)
                
                TextField("Enter 3GP file URL", text: $audioURL)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                
                // Quick test buttons for different URLs
                HStack {
                    Button("Test MP4") {
                        audioURL = "http://192.168.1.40:8000/media/documents/audio_record_1756904418365.mp4"
                    }
                    .buttonStyle(.bordered)
                    .font(.caption)
                    
                    Button("Test 3GP") {
                        audioURL = "http://192.168.1.40:8000/media/documents/audio_record_1756882218253.3gp"
                    }
                    .buttonStyle(.bordered)
                    .font(.caption)
                    
                    Button("Test Online") {
                        audioURL = "https://www.soundjay.com/misc/sounds/bell-ringing-05.wav"
                    }
                    .buttonStyle(.bordered)
                    .font(.caption)
                }
            }
            
            // Network Test Button
            Button("Test Network Connection") {
                testNetworkConnection()
            }
            .buttonStyle(.bordered)
            
            // Status
            VStack(spacing: 8) {
                Text(audioManager.statusMessage)
                    .foregroundColor(audioManager.hasError ? .red : .primary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
                
                if audioManager.isLoading {
                    ProgressView("Loading audio...")
                        .scaleEffect(0.8)
                }
            }
            
            // Progress Bar
            if audioManager.duration > 0 {
                VStack(spacing: 8) {
                    ProgressView(value: audioManager.progress)
                        .progressViewStyle(LinearProgressViewStyle())
                    
                    HStack {
                        Text(formatTime(Int(audioManager.currentTime)))
                            .font(.caption)
                            .foregroundColor(.secondary)
                        
                        Spacer()
                        
                        Text(formatTime(Int(audioManager.duration)))
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
            
            // Control Buttons
            HStack(spacing: 20) {
                Button(action: {
                    print("🎵 Play button pressed")
                    audioManager.playFromURL(audioURL)
                }) {
                    Label("Play", systemImage: "play.fill")
                        .font(.title2)
                        .frame(width: 80, height: 50)
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
                .disabled(audioManager.isLoading)
                
                Button(action: {
                    print("⏯️ Toggle button pressed")
                    audioManager.togglePlayback()
                }) {
                    Label(audioManager.isPlaying ? "Pause" : "Resume",
                          systemImage: audioManager.isPlaying ? "pause.fill" : "play.fill")
                        .font(.title2)
                        .frame(width: 80, height: 50)
                        .background(audioManager.isPlaying ? Color.orange : Color.green)
                        .foregroundColor(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
                .disabled(audioManager.player == nil)
                
                Button(action: {
                    print("⏹️ Stop button pressed")
                    audioManager.stop()
                }) {
                    Label("Stop", systemImage: "stop.fill")
                        .font(.title2)
                        .frame(width: 80, height: 50)
                        .background(Color.red)
                        .foregroundColor(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
                .disabled(audioManager.player == nil)
            }
            
            // Debug Info
            VStack(alignment: .leading, spacing: 8) {
                Text("Debug Info:")
                    .font(.headline)
                
                HStack {
                    Text("Player Status:")
                    Spacer()
                    Text(audioManager.player != nil ? "Created" : "Not Created")
                        .foregroundColor(audioManager.player != nil ? .green : .red)
                }
                
                HStack {
                    Text("Is Playing:")
                    Spacer()
                    Text(audioManager.isPlaying ? "Yes" : "No")
                        .foregroundColor(audioManager.isPlaying ? .green : .red)
                }
                
                HStack {
                    Text("Has Error:")
                    Spacer()
                    Text(audioManager.hasError ? "Yes" : "No")
                        .foregroundColor(audioManager.hasError ? .red : .green)
                }
                
                HStack {
                    Text("Duration:")
                    Spacer()
                    Text("\(formatTime(Int(audioManager.duration)))")
                }
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(8)
            
            Spacer()
        }
        .padding()
    }
    
    private func testNetworkConnection() {
        guard let url = URL(string: audioURL) else {
            print("❌ Invalid URL for network test")
            return
        }
        
        print("🌐 Testing network connection to: \(url)")
        
        var request = URLRequest(url: url)
        request.httpMethod = "HEAD"
        request.timeoutInterval = 10.0
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    print("❌ Network test failed: \(error.localizedDescription)")
                    return
                }
                
                if let httpResponse = response as? HTTPURLResponse {
                    print("✅ Network test successful!")
                    print("📡 Status Code: \(httpResponse.statusCode)")
                    print("📋 Headers: \(httpResponse.allHeaderFields)")
                    
                    if let contentType = httpResponse.allHeaderFields["Content-Type"] as? String {
                        print("🎵 Content Type: \(contentType)")
                    }
                    
                    if let contentLength = httpResponse.allHeaderFields["Content-Length"] as? String {
                        print("📏 Content Length: \(contentLength) bytes")
                    }
                }
            }
        }.resume()
    }
    
    private func formatTime(_ seconds: Int) -> String {
        let minutes = seconds / 60
        let remainingSeconds = seconds % 60
        return String(format: "%d:%02d", minutes, remainingSeconds)
    }
}

// MARK: - Compact Player View
struct CompactAudioPlayerView: View {
    @StateObject private var audioManager = AudioPlayerManager()
    let audioURL: String
    
    init(url: String) {
        self.audioURL = url
    }
    
    var body: some View {
        HStack {
            Button(action: {
                if audioManager.isPlaying {
                    audioManager.togglePlayback()
                } else {
                    audioManager.playFromURL(audioURL)
                }
            }) {
                Image(systemName: audioManager.isPlaying ? "pause.circle.fill" : "play.circle.fill")
                    .font(.title)
                    .foregroundColor(.blue)
            }
            .disabled(audioManager.isLoading)
            
            VStack(alignment: .leading, spacing: 4) {
                Text("Audio Player")
                    .font(.headline)
                
                Text(audioManager.statusMessage)
                    .font(.caption)
                    .foregroundColor(audioManager.hasError ? .red : .secondary)
                    .lineLimit(2)
            }
            
            Spacer()
            
            if audioManager.isLoading {
                ProgressView()
                    .scaleEffect(0.8)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}



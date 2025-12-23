//
//  ChatRoomView.swift
//  iosApp
//
//  Created by Vinu on 01/09/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import AVKit
import shared
import SwiftUI

struct ChatRoomView: View {

    @ObservedObject private(set) var viewModel: ContentView.ViewModel
    var onBackPressed: () -> Void
    @State var toastMessage = "Loading..."
    init(viewModel: ContentView.ViewModel, onBackPressed: @escaping () -> Void) {
        self.viewModel = viewModel
        self.onBackPressed = onBackPressed
    }

    var body: some View {
        VStack {
            HeaderView
          MessageListView
            UserEntryFieldView
        }
        .ignoresSafeArea(edges: .top)
        .navigationBarBackButtonHidden()
        .onAppear(perform: {
            viewModel.initChatRoomSocketConnection(roomId: viewModel.selectedRoom!.roomID!)
        })
    }

    var MessageListView: some View {
        VStack {
            List {
                VStack {
                    ForEach(viewModel.messageList.reversed(), id: \.self) { message in

                        let color = if message.user != currentUser { Color.theme.secondary_color } else { Color.theme.primary_color }
                        let alignment = if message.user != currentUser { Alignment.leading } else { Alignment.trailing }
                        HStack {
                            if !message.message.isEmpty {
                                CommanText(textValue: message.message, textColor: .white)
                                    .body.padding(10)
                                    .background(color.cornerRadius(10))
                                    .foregroundColor(.white)
                                    .frame(width: 200)
                                    .padding(1)
                            }
                            if message.chatAttachment != nil {
                                if let notOptionalChatAttachment = message.chatAttachment {
                                    AttachmentView(chatAttachment:notOptionalChatAttachment)
                                        .padding(10)

                                        .background(color.cornerRadius(10))
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                        .padding(1)
                                }
                            }
                        }
                        .frame(maxWidth: .infinity, alignment: alignment)
                    }
                }
                .listRowBackground(Color.clear)
                .padding(EdgeInsets(top: 0, leading: -20, bottom: 0, trailing: -20))
            }
            .listSectionSeparator(.hidden, edges: .bottom)
            .listStyle(PlainListStyle())
        }
    }
    var UserEntryFieldView: some View {
        HStack {
            TextField("Enter Message", text: $viewModel.messageToSend)
                .padding(15)

                .foregroundColor(Color.theme.secondary_color)
            Image(systemName: "paperplane.fill")
                .background(.clear)
                .foregroundColor(.black)
                .padding(.trailing, 20)
                .onTapGesture {
                    print("send message")
                    if !viewModel.messageToSend.isEmpty {
                        let sendMessage = ChatMessageRequest(command: "content", message: viewModel.messageToSend, user: currentUser, pageNumber: 1, blocked_user: [], chatAttachment: nil)
                        viewModel.messageToSend = ""
                        viewModel.sendMessage(messageToSendJSON: sendMessage.getStringData())
                    }
                }
        }
        .background(Color.theme.textField_background)
    }

    var HeaderView: some View {
        HStack {
            Image(systemName: "chevron.left")
                .foregroundColor(.white)
            Text(viewModel.selectedRoom!.roomName)
                .foregroundColor(.white)
                .font(.custom(Font.family.JURA_BOLD, size: Font.size.title_large_font_size))
        }.padding(.top, 60)
            .padding(15)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.theme.primary_color, alignment: .leading)
            .onTapGesture {
                self.onBackPressed()
            }
    }

    struct AttachmentView: View {
        @State private var isAudioFilePlaying = false

        @State private var audioPlayer: AVPlayer?
        
        var chatAttachment: ChatAttachment
        init(chatAttachment: ChatAttachment) {
            self.chatAttachment = chatAttachment
        }

        var body: some View {
            HStack {
                switch self.chatAttachment {
                case is VoiceAttachment:
                    let voiceAttachment = self.chatAttachment as! VoiceAttachment
                    if let  notOptionalFile=voiceAttachment.voiceAttachment.getUploadFile()
                    {
                        let _=print(notOptionalFile)
                        AudioPlayerView()
                      
                    }
                  
                default:
                    Text("default")
                }
            }
        }
    }
}

struct CommanText: View {
    var textValue: String
    var textColor: Color
    init(textValue: String, textColor: Color) {
        self.textValue = textValue
        self.textColor = textColor
    }

    var body: some View {
        Text(self.textValue)
            .padding(8)

            .bold()
            .frame(maxWidth: .infinity, alignment: .leading)
            .foregroundColor(textColor)
            .font(.custom(Font.family.JURA_BOLD, size: 20))
    }
}

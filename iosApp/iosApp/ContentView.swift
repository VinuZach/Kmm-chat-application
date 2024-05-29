import SwiftUI
import SimpleToast
import shared
struct ContentView: View {
    @ObservedObject private(set) var viewModel: ViewModel
    
    private let toastOptions = SimpleToastOptions(alignment: .top,
                                                  hideAfter: 2,
                                                  
                                                  animation: .default,
                                                  modifierType: .slide
    )
    
    var body: some View {
        NavigationView
        {
            
            VStack
            {
                List() {
                    ForEach(viewModel.chatMessageList, id: \.self) { messageItem in
                        Text(messageItem.message)
                            .padding(10)
                    }
                }
                HStack
                {
                    TextField("type message", text: $viewModel.sendMessageData)
                        .keyboardType(.asciiCapable)
                    Button("send")
                    {
                        print(viewModel.sendMessage)
                        viewModel.sendMessage()
                    }
                }.padding(.horizontal,30)
            }
            
        }.navigationBarBackButtonHidden(true)
            .onAppear { print("🔴 OnAppear") }
            .onDisappear { print("🔴 OnDisappear") }
    }
}


#Preview {
    ContentView(viewModel: ContentView.ViewModel())
}
extension ContentView {
    class ViewModel: ObservableObject {
        @Published var chatMessageList=[MessageDto]()
        @Published var sendMessageData = ""
        var isWebSocketConnect = false
        var webSocket = Greeting().provideChatSocketService()
        init() {
            initSocketConnection()
        }
        
        func initSocketConnection()  {
            DispatchQueue.main.async { [self] in
                webSocket.doInitSession(roomId: "/43/",completionHandler:
                                            { [self] resource,error in
                    
                    self.isWebSocketConnect = (resource is ResourceSuccess)
                    if resource is ResourceSuccess
                    {
                        
                        print("aaaaa")
                        webSocket.observeMessages().watch(block:{
                            message in
                            if message != nil
                            {
                                self.chatMessageList.append(message!)
                            }
                        })
                    }
                    
                })
                
            }
        }
        struct Message :Encodable
        {
            var command:String
            var message:String
            var user:String
            var pageNumber:Int
            var blocked_user:[String]
            
        }
        func sendMessage()  {
            if isWebSocketConnect
            {
                let messageToSend = Message(command: "content", message: sendMessageData, user: "ccc@ccc.com", pageNumber: 1, blocked_user:  [String]())
                do {
                    let jsonData = try JSONEncoder().encode(messageToSend)
                    let jsonString = String(data: jsonData, encoding: .utf8)!
                    print(jsonString) // [{"sentence":"Hello world","lang":"en"},{"sentence":"Hallo Welt","lang":"de"}]
                    
                    webSocket.sendMessage(message: jsonString, completionHandler: {
                        error in
                        print(error)
                        self.sendMessageData=""
                    })
                } catch { print(error) }

            }
        }
    }
}

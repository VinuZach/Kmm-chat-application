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
                    TextField("type message", text: $viewModel.sendMessage)
                        .keyboardType(.asciiCapable)
                    Button("send")
                    {
                        print(viewModel.sendMessage)
                        viewModel.sendMessage(message: viewModel.sendMessage)
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
        @Published var sendMessage = ""
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
        func sendMessage(message:String)  {
            if isWebSocketConnect
            {
                let messageToSend = Message(command: "content", message: message, user: "ccc@ccc.com", pageNumber: 1, blocked_user:  [String]())
                do {
                    let jsonData = try JSONEncoder().encode(messageToSend)
                    let jsonString = String(data: jsonData, encoding: .utf8)!
                    print(jsonString) // [{"sentence":"Hello world","lang":"en"},{"sentence":"Hallo Welt","lang":"de"}]
                    
                    webSocket.sendMessage(message: jsonString, completionHandler: {
                        error in
                        print(error)
              
                    })
                } catch { print(error) }
//                let dd="{\n" +
//                    "                   \"command\": \"content\",\n" +
//                    "                   \"message\": \"message \",\n" +
//                    "                   \"user\":  \"ccc@ccc.com\",\n" +
//                    "                   \"pageNumber\":1,\n" +
//                    "                   \"blocked_user\":[]\n" +
//                    "               }"
           
            }
        }
    }
}

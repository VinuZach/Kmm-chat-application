import SwiftUI
import shared
import UniformTypeIdentifiers

extension UTType
{
    static let chatItemDetails = UTType(exportedAs: "orgIdentifier.iosApp")
}

struct ContentView: View {
    @ObservedObject private(set) var viewModel: ViewModel
    @State var showDialog:Bool=false
 
    
    var body: some View {
        
        NavigationStack(path:$viewModel.navigationPath)
        {
            groupListingView
          
            .navigationDestination(for: ChatRoomWithTotalMessage.self)
            { destination in
                
               let _ = print(destination)
               
                VStack
                {
                    HStack
                    {
                        Text(destination.roomName)
                    }
                        List() {
                        ForEach(viewModel.chatMessageList.reversed(), id: \.self) { messageItem in
                            Text(messageItem.message)
                                .padding(10)
                                .rotationEffect(.radians(.pi))
                                .scaleEffect(x: -1, y: 1, anchor: .center)
                        }
                    }.rotationEffect(.radians(.pi))
                        .scaleEffect(x: -1, y: 1, anchor: .center)
                    HStack
                    {
                        TextField("type message", text: $viewModel.sendMessageData)
                            .keyboardType(.asciiCapable)
                        Button("send")
                        {
                           

                            let messageToSend=ChatMessageRequest(command: "content", message: viewModel.sendMessageData, user: "aaa@aaa.com", pageNumber: 1, blocked_user: [String]())
                            viewModel.sendMessage(messageToSendJSON: messageToSend.getStringData())
                        }
                    }.padding(.horizontal,30)
                }
                .onAppear(perform: {
                    viewModel.initChatRoomSocketConnection(roomId: destination.roomID!)
                })
            }
        }
                  
 
    }
    
  
    
    var groupListingView:some View
    {
     
       
        VStack
        {
            let groupDetailsList = viewModel.groupListingWithChat?.clusterRoomGroups ?? [ChatRoomWithTotalMessage]()
            
            ScrollView() {
                LazyHStack() {
                     ForEach(groupDetailsList,id: \.clusterGroupId)
                    {
                        groupDetails in
                        Text(groupDetails.roomName)
                            .overlay(RoundedRectangle(cornerRadius: 5)
                                        .stroke(.gray, lineWidth: 1))
                            .onTapGesture {
                                viewModel.retrieveGroupDetails(groupDetails: groupDetails)
                            }
                            .dropDestination(for:ChatItemDetails.self){ droppedRoomDetailList,location in
                          
                                viewModel.reassignRoomToGroup=ReassignRoomToGroup(selectedRoom: droppedRoomDetailList.first!, selectedGroup: groupDetails)
                                print(groupDetails)
                                showDialog=true
                                return false
                                
                            }.alert(isPresented: $showDialog, content: {
                               
                                var selectedRoom=viewModel.reassignRoomToGroup!.selectedRoom
                                var selectedGroup=viewModel.reassignRoomToGroup!.selectedGroup
                                let title = if((selectedRoom.clusterGroupId) != "None")
                                {
                                    "Reassign to "+selectedGroup.roomName
                                } else
                                {
                                    "Assign room to "+selectedGroup.roomName
                                }
                                
                                let message = if((selectedRoom.clusterGroupId) != "None")
                                {
                                    if(selectedGroup.roomCountUnderGroup-1 == 0)
                                    {
                                        "Group will be deleted ..Reassign room?"
                                    }
                                    else
                                    {
                                        "Reassign group?"
                                    }
                                } else
                                {
                                    "Assign room to group"
                                }
                                return Alert(title: Text(title),
                                             message: Text(message),
                                             primaryButton: .default(Text("Confirm"))
                                             {
                                  
                                    viewModel.assignRoomToSelectedGroup(groupID: selectedGroup.clusterGroupId!,
                                                                        roomId: Int32(selectedRoom.roomId))
                                            viewModel.reassignRoomToGroup=nil
                                            },
                                             secondaryButton: .cancel())
                            })
                    }

                     .listStyle(.plain)
                }
              
            }.frame(maxWidth:.infinity)
                .background(.red)
            let chatMessageList = viewModel.groupListingWithChat?.chatRoomWithTotalMessage ??
            [ChatRoomWithTotalMessage]()
            
            if viewModel.selectedRoomGroup != nil
            {
                ZStack
                {
                    HStack{
                        Image(systemName: "chevron.backward")
                            .onTapGesture {
                                viewModel.retrieveGroupDetails(groupDetails: nil)
                            }.padding(.horizontal,20)
                        Text(viewModel.selectedRoomGroup!.roomName)
                        Spacer()
                    }
                    .padding(.vertical,10)
                    
                }
            }
            List(chatMessageList,id: \.roomID) {
                chatMessageDetails in
                let chatDetails = ChatItemDetails(roomId: chatMessageDetails.roomID as! Int, roomName: chatMessageDetails.roomName, clusterGroupId: chatMessageDetails.clusterGroupId, totalMessage: chatMessageDetails.totalMessages, roomCountUnderGroup: chatMessageDetails.roomCountUnderGroup)
                
                Text(chatMessageDetails.roomName)
                   
                    .onTapGesture {
                        viewModel.navigationPath.append(chatMessageDetails)
                    }.draggable(chatDetails)
                    
            }
            
        }
        .onAppear(perform: {
            print("group appear ")
            viewModel.initGroupListingSocketConnection()
        })
        
    }
}

#Preview {
    ContentView(viewModel: ContentView.ViewModel())
}

struct ReassignRoomToGroup
{
    var selectedRoom:ChatItemDetails
    var selectedGroup:ChatRoomWithTotalMessage
    
    init(selectedRoom: ChatItemDetails, selectedGroup: ChatRoomWithTotalMessage) {
        self.selectedRoom = selectedRoom
        self.selectedGroup = selectedGroup
    }
}

struct ChatItemDetails:Codable,Transferable{
    let roomId:Int
    let roomName:String
    let clusterGroupId:String?
    let totalMessage:Int32
    let roomCountUnderGroup:Int32
    
    static var transferRepresentation: some TransferRepresentation
    {
        CodableRepresentation(contentType: .chatItemDetails)
    }

}

extension ContentView {
    class ViewModel: ObservableObject {
        @Published var chatMessageList=[MessageDto]()
        @Published var sendMessageData = ""
        @Published var groupListingWithChat :GroupDetailsResponseDto?
        var isWebSocketConnect = false
        @Published var navigationPath = [ChatRoomWithTotalMessage]()
        var selectedRoomGroup: ChatRoomWithTotalMessage? = nil
        var webSocket = Greeting().provideChatSocketService()
     
        var reassignRoomToGroup:ReassignRoomToGroup?=nil
        
        
      
        
        func assignRoomToSelectedGroup(groupID:String,roomId:Int32) {
            DispatchQueue.main.async {
                ApiHandler(apiCallManager: Greeting().getHttpClientForApi1())
                    .assignRoomToSelectedGroup(roomId: roomId, groupId: Int32(groupID)!, userOverride: true){
                        (isSuccess, result) in
                        print(isSuccess)
                        print(result)
                    }
                    completionHandler: { err in
                    print("cccc")
                }
            }
        }
        func initGroupListingSocketConnection()  {
            DispatchQueue.main.async {
                self.initSocketConnection(webSocketLink: "/chatList", isForChat: false){
                  print("aaaaa")
                    self.retrieveGroupDetails(groupDetails: nil)

                }
                
            }
        }
        
        func initChatRoomSocketConnection(roomId:KotlinInt)  {
            DispatchQueue.main.async {
                self.initSocketConnection(webSocketLink: "/\(roomId)/", isForChat: true){
                  print("aaaaa")
            
                }
                
            }
        }
        
        func retrieveGroupDetails(groupDetails:ChatRoomWithTotalMessage?)  {
            selectedRoomGroup=groupDetails
            let clusterIdString :String = groupDetails?.clusterGroupId ?? "-1"
            let clusterId:Int32 = Int32(clusterIdString) ?? -1
            
            let groupListRequestData=GroupListRequestData(user: "aaa@aaa.com", clusterId:clusterId)
       
        print(groupListRequestData.groupListToString())
        self.sendMessage(messageToSendJSON: groupListRequestData.groupListToString())
        }
        
        
        func initSocketConnection(webSocketLink:String,isForChat:Bool, onConnected: @escaping()->())  {
            DispatchQueue.main.async { [self] in
                webSocket.doInitSession(roomId: webSocketLink,completionHandler:
                                            { [self] resource,error in
                    
                    self.isWebSocketConnect = (resource is ResourceSuccess)
                    if resource is ResourceSuccess
                    {
                      
                        
                        if isForChat
                        {
                            webSocket.observeMessages().watch(block:{
                                message in
                                if message != nil
                                {
                                    if !self.chatMessageList.contains(message!)
                                    {
                                        self.chatMessageList.append(message!)
                                    }
                                   
                                }
                            })
                        }
                        else
                        {
                            
                            webSocket.observeGroupList().watch(block: {
                                
                                groupListDetails in
                             
                                if groupListDetails != nil
                                {
                                    self.groupListingWithChat=groupListDetails ?? nil
                                }
                            })
                        }
                        onConnected()
                    }
                    
                })
                
            }
        }
   
        func sendMessage(messageToSendJSON:String)  {
            if isWebSocketConnect
            {
                DispatchQueue.main.async {
                    self.webSocket.sendMessage(message: messageToSendJSON, completionHandler: {
                        error in
                        print(error ?? "")
                        self.sendMessageData=""
                    })
                }

            }
        }
    
    }
   
}

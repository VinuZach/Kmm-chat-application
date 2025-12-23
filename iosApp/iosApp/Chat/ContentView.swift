import shared
import SwiftUI
import UniformTypeIdentifiers
import AVKit

extension UTType {
    static let chatItemDetails = UTType(exportedAs: "orgIdentifier.iosApp")
}

var currentUser: String = ""

struct ContentView: View {
    @ObservedObject private(set) var viewModel: ContentView.ViewModel
    @State var showDialog: Bool = false
    @State var redirectToChat: Bool = false
    @State var createNewChat: Bool = false
    @State var createNewGroup: Bool = false

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottomTrailing) {
                VStack {
                    Text("Logo")
                        .frame(minHeight: 150)

                    ChatGroupAndListingMain
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
                .background(Color.theme.primary_color)
                Button(action: {
                    createNewChat.toggle()

                }, label: {
                    Image(systemName: "plus").padding(20)
                }).frame(alignment: .bottomTrailing)
                    .foregroundColor(.white)
                    .background(Color.theme.primary_color.cornerRadius(60))
                    .padding()
            }
        }.navigationDestination(isPresented: $redirectToChat) {
            ChatRoomView(viewModel: viewModel) {
                print("asdasd")
                redirectToChat.toggle()
            }
        }
        .navigationDestination(isPresented: $createNewChat) {
            CreateChatOrGroupView(issForChatCreation: true) {
                createNewChat.toggle()
            }.navigationBarBackButtonHidden()
        }

        .navigationDestination(isPresented: $createNewGroup) {
            CreateChatOrGroupView(issForChatCreation: false) {
                createNewGroup.toggle()
            }.navigationBarBackButtonHidden()
        }
    }

 




    var ChatGroupAndListingMain: some View {
        return VStack {
            let groupDetailsList = viewModel.groupListingWithChat?.clusterRoomGroups ?? [ChatRoomWithTotalMessage]()

            ScrollView(.horizontal) {
                HStack {
                    Section {
                        Text("+").padding(.horizontal, 40)
                            .padding(.vertical, 8)
                            .foregroundColor(Color.theme.textField_background)
                            .background(Color.theme.primary_color.cornerRadius(20)).onTapGesture {
                                createNewGroup = true
                            }
                    }
                    ForEach(groupDetailsList, id: \.clusterGroupId) {
                        groupDetails in

                        @State var isTargetted: Bool = false

                        Text(groupDetails.roomName)
                            .padding(.horizontal, 30)
                            .padding(.vertical, 7)
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Color.theme.primary_color)
                            )

                            .onTapGesture {
                                viewModel.retrieveGroupDetails(groupDetails: groupDetails)
                            }

                            .dropDestination(for: ChatItemDetails.self) { droppedRoomDetailList, _ in

                                viewModel.reassignRoomToGroup = ReassignRoomToGroup(selectedRoom: droppedRoomDetailList.first!, selectedGroup: groupDetails)
                                print(groupDetails)
                                showDialog = true
                                return false

                            }.alert(isPresented: $showDialog, content: {
                                let selectedRoom = viewModel.reassignRoomToGroup!.selectedRoom
                                let selectedGroup = viewModel.reassignRoomToGroup!.selectedGroup
                                let title = if (selectedRoom.clusterGroupId) != "None" {
                                    "Reassign to " + selectedGroup.roomName
                                } else {
                                    "Assign room to " + selectedGroup.roomName
                                }

                                let message = if (selectedRoom.clusterGroupId) != "None" {
                                    if selectedGroup.roomCountUnderGroup - 1 == 0 {
                                        "Group will be deleted ..Reassign room?"
                                    } else {
                                        "Reassign group?"
                                    }
                                } else {
                                    "Assign room to group"
                                }
                                return Alert(title: Text(title),
                                             message: Text(message),
                                             primaryButton: .default(Text("Confirm")) {
                                                 viewModel.assignRoomToSelectedGroup(groupID: selectedGroup.clusterGroupId!,
                                                                                     roomId: Int32(selectedRoom.roomId))
                                                 viewModel.reassignRoomToGroup = nil
                                             },
                                             secondaryButton: .cancel())
                            })
                    }
                }

                .padding()
            }

            let chatMessageList = viewModel.groupListingWithChat?.chatRoomWithTotalMessage ??
                [ChatRoomWithTotalMessage]()

            if viewModel.selectedGroup != nil {
                ZStack {
                    HStack {
                        Image(systemName: "chevron.backward")
                            .padding(.horizontal, 20)
                        Text(viewModel.selectedGroup!.roomName)
                        Spacer()
                    }
                    .padding(.vertical, 20)
                    .foregroundColor(.white)
                    .background(Color.theme.primary_color)
                    .onTapGesture {
                        viewModel.retrieveGroupDetails(groupDetails: nil)
                    }

                    .padding(.vertical, 10)
                }
            }
            List(chatMessageList, id: \.roomID) {
                chatMessageDetails in
                let chatDetails = ChatItemDetails(roomId: chatMessageDetails.roomID as! Int, roomName: chatMessageDetails.roomName, clusterGroupId: chatMessageDetails.clusterGroupId, totalMessage: chatMessageDetails.totalMessages, roomCountUnderGroup: chatMessageDetails.roomCountUnderGroup)
                HStack {
                    Text("")
                    CommanText(textValue: chatMessageDetails.roomName, textColor: Color.theme.secondary_color)
                        .onTapGesture {
                            viewModel.selectedRoom = chatMessageDetails
                            redirectToChat.toggle()
                        }.draggable(chatDetails)
                }
                .listRowBackground(Color.clear)
                .padding(EdgeInsets(top: 0, leading: -20, bottom: 0, trailing: -20))
            }
            .listSectionSeparator(.hidden, edges: .bottom)
            .listStyle(PlainListStyle())
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(Color.theme.backgroundColor.cornerRadius(20).ignoresSafeArea(edges: .bottom))
        .onAppear(perform: {
            print("group appear ")
            let cacheManger = CacheManager(dataStoreInstance: DataStoreInstance().getManger())
            cacheManger.retrieveStringDataFromCache(key: cacheManger.USER_NAME) {
                result, _ in
                if result != nil, let result {
                    print(currentUser)
                    currentUser = result
                    print(currentUser)
                    viewModel.initGroupListingSocketConnection()
                }
            }

        })
    }

    var groupListingView: some View {
        VStack {
            let groupDetailsList = viewModel.groupListingWithChat?.clusterRoomGroups ?? [ChatRoomWithTotalMessage]()

            ScrollView {
                LazyHStack {
                    ForEach(groupDetailsList, id: \.clusterGroupId) {
                        groupDetails in
                        Text(groupDetails.roomName)
                            .overlay(RoundedRectangle(cornerRadius: 5)
                                .stroke(.gray, lineWidth: 1))
                            .onTapGesture {
                                viewModel.retrieveGroupDetails(groupDetails: groupDetails)
                            }
                            .dropDestination(for: ChatItemDetails.self) { droppedRoomDetailList, _ in

                                viewModel.reassignRoomToGroup = ReassignRoomToGroup(selectedRoom: droppedRoomDetailList.first!, selectedGroup: groupDetails)

                                showDialog = true
                                return false

                            }.alert(isPresented: $showDialog, content: {
                                let selectedRoom = viewModel.reassignRoomToGroup!.selectedRoom
                                let selectedGroup = viewModel.reassignRoomToGroup!.selectedGroup
                                let title = if (selectedRoom.clusterGroupId) != "None" {
                                    "Reassign to " + selectedGroup.roomName
                                } else {
                                    "Assign room to " + selectedGroup.roomName
                                }

                                let message = if (selectedRoom.clusterGroupId) != "None" {
                                    if selectedGroup.roomCountUnderGroup - 1 == 0 {
                                        "Group will be deleted ..Reassign room?"
                                    } else {
                                        "Reassign group?"
                                    }
                                } else {
                                    "Assign room to group"
                                }
                                return Alert(title: Text(title),
                                             message: Text(message),
                                             primaryButton: .default(Text("Confirm")) {
                                                 viewModel.assignRoomToSelectedGroup(groupID: selectedGroup.clusterGroupId!,
                                                                                     roomId: Int32(selectedRoom.roomId))
                                                 viewModel.reassignRoomToGroup = nil
                                             },
                                             secondaryButton: .cancel())
                            })
                    }

                    .listStyle(.plain)
                }

            }.frame(maxWidth: .infinity)
                .background(.red)
            let chatMessageList = viewModel.groupListingWithChat?.chatRoomWithTotalMessage ??
                [ChatRoomWithTotalMessage]()

            if viewModel.selectedGroup != nil {
                ZStack {
                    HStack {
                        Image(systemName: "chevron.backward")
                            .onTapGesture {
                                viewModel.retrieveGroupDetails(groupDetails: nil)
                            }.padding(.horizontal, 20)
                        Text(viewModel.selectedGroup!.roomName)
                        Spacer()
                    }
                    .padding(.vertical, 10)
                }
            }
            List(chatMessageList, id: \.roomID) {
                chatMessageDetails in
                let chatDetails = ChatItemDetails(roomId: chatMessageDetails.roomID as! Int, roomName: chatMessageDetails.roomName, clusterGroupId: chatMessageDetails.clusterGroupId, totalMessage: chatMessageDetails.totalMessages, roomCountUnderGroup: chatMessageDetails.roomCountUnderGroup)

                Text(chatMessageDetails.roomName)

                    .onTapGesture {
                        print("asdsad")
                        redirectToChat.toggle()
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

struct ReassignRoomToGroup {
    var selectedRoom: ChatItemDetails
    var selectedGroup: ChatRoomWithTotalMessage

    init(selectedRoom: ChatItemDetails, selectedGroup: ChatRoomWithTotalMessage) {
        self.selectedRoom = selectedRoom
        self.selectedGroup = selectedGroup
    }
}

struct ChatItemDetails: Codable, Transferable {
    let roomId: Int
    let roomName: String
    let clusterGroupId: String?
    let totalMessage: Int32
    let roomCountUnderGroup: Int32

    static var transferRepresentation: some TransferRepresentation {
        CodableRepresentation(contentType: .chatItemDetails)
    }
}

extension ContentView {
    class ViewModel: ObservableObject {
        @Published var chatMessageList: MessageDto?
        @Published var sendMessageData = ""
        @Published var messageList: [MessageDto.PrevMessage] = []
        @Published var messageToSend = ""
        @Published var groupListingWithChat: GroupDetailsResponseDto?
        var isWebSocketConnect = false
        @Published var navigationPath = [ChatRoomWithTotalMessage]()
        var selectedGroup: ChatRoomWithTotalMessage?
        var selectedRoom: ChatRoomWithTotalMessage?
        var webSocket = Greeting().provideChatSocketService()

        var reassignRoomToGroup: ReassignRoomToGroup?

        func assignRoomToSelectedGroup(groupID: String, roomId: Int32) {
            DispatchQueue.main.async {
                ApiHandler(apiCallManager: Greeting().getHttpClientForApi1())
                    .assignRoomToSelectedGroup(roomId: roomId, groupId: Int32(groupID)!, userOverride: true) {
                        isSuccess, result in
                        print(isSuccess)
                        print(result)
                    }
            completionHandler: { _ in
                        print("cccc")
                    }
            }
        }

        func initGroupListingSocketConnection() {
            DispatchQueue.main.async {
                self.initSocketConnection(webSocketLink: "/chatList", isForChat: false) {
                    print("2222")
                    self.retrieveGroupDetails(groupDetails: nil)
                }
            }
        }

        func initChatRoomSocketConnection(roomId: KotlinInt) {
            messageList.removeAll()
            DispatchQueue.main.async {
                self.initSocketConnection(webSocketLink: "/\(roomId)/", isForChat: true) {
                    let chatMessageRequest = ChatMessageRequest(command: "join", message: "", user: currentUser, pageNumber: 0, blocked_user: [], chatAttachment: nil)
                    self.sendMessage(messageToSendJSON: chatMessageRequest.getStringData())
                }
            }
        }

        func retrieveGroupDetails(groupDetails: ChatRoomWithTotalMessage?) {
            selectedGroup = groupDetails
            let clusterIdString: String = groupDetails?.clusterGroupId ?? "-1"
            let clusterId: Int32 = Int32(clusterIdString) ?? -1

            let groupListRequestData = GroupListRequestData(user: currentUser, clusterId: clusterId)

            print(groupListRequestData.groupListToString())
            sendMessage(messageToSendJSON: groupListRequestData.groupListToString())
        }

        func initSocketConnection(webSocketLink: String, isForChat: Bool, onConnected: @escaping () -> Void) {
            DispatchQueue.main.async { [self] in
                webSocket.doInitSession(roomId: webSocketLink, currentUserName: currentUser, completionHandler:
                    { [self] resource, _ in

                        self.isWebSocketConnect = (resource is ResourceSuccess)
                        if resource is ResourceSuccess {
                            if isForChat {
                                webSocket.observeMessages().watch(block: {
                                    message in

                                    if let notNullMessage = message {
                                        if let notNNullPrevMessages = notNullMessage.prevMessages {
                                            if !notNNullPrevMessages.isEmpty {
                                                self.messageList.append(contentsOf: notNNullPrevMessages)
                                            }
                                        }
                                        if !notNullMessage.message.isEmpty || notNullMessage.chatAttachment != nil {
                                            self.messageList.insert(MessageDto.PrevMessage(message: notNullMessage.message, timestamp: "", user: notNullMessage.user, blocked_user: notNullMessage.blocked_user, chatAttachment: notNullMessage.chatAttachment), at: 0)
                                        }
                                    }

                                })
                            } else {
                                webSocket.observeGroupList().watch(block: {
                                    groupListDetails in

                                    if groupListDetails != nil {
                                        if let requestedUser = groupListDetails?.requested_user, requestedUser == currentUser {
                                            self.groupListingWithChat = groupListDetails ?? nil
                                        }
                                    }
                                })
                            }
                            onConnected()
                        }

                    })
            }
        }

        func sendMessage(messageToSendJSON: String) {
            print(isWebSocketConnect)
            if isWebSocketConnect {
                DispatchQueue.main.async {
                    self.webSocket.sendMessage(message: messageToSendJSON, completionHandler: {
                        error in
                        print(error ?? "")
                        self.sendMessageData = ""
                    })
                }
            }
        }
    }
}

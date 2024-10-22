//
//  CreateChatOrGroupView.swift
//  iosApp
//
//  Created by Vinu on 18/10/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import shared
struct CreateChatOrGroupView:View
{
    
    @StateObject var viewModel=CreateChatOrGroupViewModel()
    var onBackPressed: ()->()
    var isForChatCreation:Bool
    
    init(issForChatCreation:Bool,onbackPressed:@escaping()->()){
        self.onBackPressed=onbackPressed
        self.isForChatCreation=issForChatCreation
        
    }
    var body:some View
    {
        
        VStack
        {
            
            HStack
            {
                Image(systemName: "chevron.left")
                    .foregroundColor(.white)
                let titleText = isForChatCreation ? "New Converstation": "New Group"
                Text(titleText)
                    .foregroundColor(.white)
                    .font(.custom(Font.family.JURA_BOLD, size: Font.size.title_large_font_size))
            }.padding(.top,60)
                .padding(15)
                .frame(maxWidth: .infinity,alignment: .leading)
                .background(Color.theme.primary_color,alignment: .leading)
                .onTapGesture {
                    self.onBackPressed()
                }
       
            
        }.frame(maxHeight: .infinity,alignment: .top)
            .onAppear(perform: {
                if self.isForChatCreation
                {
                    var cacheManger=CacheManager(dataStoreInstance: DataStoreInstance().getManger())
                  
                    cacheManger.retrieveStringDataFromCache(key: cacheManger.USER_NAME) {
                        result,error in
                        if result != nil,let result
                        {
                            DispatchQueue.main.async
                            {
                                self.viewModel.retrieveUserEmailList(currentUserName: result)
                            }
                    
                        }
                        
                    }
                }
            })
            .ignoresSafeArea()
        
    }
    
    
}

class CreateChatOrGroupViewModel : ObservableObject {
    @Published var allUserNameList = [String]()
    let apiHandler=ApiHandler(apiCallManager:  Greeting().getHttpClientForApi1())
    init() {
        
    }
    
    func retrieveUserEmailList(currentUserName:String)  {
        apiHandler.retrieveAllUserEmails(currentUserName: currentUserName) {   (isSuccess,result) in
            if(isSuccess.boolValue)
            {
                let res=result as! UsersEmailsResponse
                self.allUserNameList=res.userEmailList
                print(self.$allUserNameList)
            }
            print(result)
        } completionHandler: { error in
           
        }
        
    }
}


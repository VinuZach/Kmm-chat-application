//
//  UserAuthentication.swift
//  iosApp
//
//  Created by Vinu on 28/05/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import SimpleToast
import shared


struct UserAuthentication: View {
    
    @ObservedObject private(set) var viewModel: ViewModel
    private let toastOptions = SimpleToastOptions(alignment: .top,
                                                  hideAfter: 2,
                                        
                                                  animation: .default,
                                                  modifierType: .slide
    )

    struct LoginView:View {
        var body: some View
        {
            VStack
            {
                Text("asdasd")
            }.background(.blue)
        }
    }
    
    var body: some View {
      
        NavigationStack(path: $viewModel.navigationPath)
        {
            VStack
            {
                VStack
                {
                    Text("asdasdasd")
                }
            
                .background(Color(.white))
                    
                LoginView().frame(width: .infinity)
            }
            .frame(maxWidth: .infinity,  maxHeight: .infinity,alignment: .bottom )
            .background(Color.red)
          
//            VStack(alignment: .center, spacing: 8)
//               {
//                   
//                   if(!viewModel.isUserLoginView)
//                   {
//                       TextField("Email",text: $viewModel.email)  .padding(10)
//                           .overlay(RoundedRectangle(cornerRadius: 4).stroke(Color.gray, lineWidth: 2))
//                   }
//                   TextField("Username",text: $viewModel.username)  .padding(10)
//                       .overlay(RoundedRectangle(cornerRadius: 4).stroke(Color.gray, lineWidth: 2))
//                   
//                   TextField("Password",text: $viewModel.password).padding(10)
//                       .overlay(RoundedRectangle(cornerRadius: 4).stroke(Color.gray, lineWidth: 2))
//
//                   
//                   Text("Forgot Password").font(.custom("reset passord", size: 12)).frame(maxWidth: .infinity,alignment:.trailing)
//               
//                   Button(action: {
//                       print(viewModel.isUserLoginView)
//                  
//                       if(viewModel.isUserLoginView)
//                       {
//                           ApiHandler(apiCallManager: Greeting().getHttpClientForApi1()).verifyUserDetails(userName:viewModel.username, password: viewModel.password) { (isSuccess, result) in
//                               
//                               let res=result as! UserAuthenticationResponse
//                               if(!res.success)
//                               {
//                                   viewModel.toastMessage=res.message!
//                                   viewModel.showToast=true
//                               }
//                               else
//                               {
//                                   viewModel.toastMessage="Login successfull"
//                                   viewModel.showToast=true
//                                   viewModel.navigationPath.removeAll()
//                                   viewModel.navigationPath.append("aaaa")
//                               }
//                               
//                               
//                           } completionHandler: { err in
//                               print("cccc")
//                           }
//                       }else
//                       {
//                           ApiHandler(apiCallManager: Greeting().getHttpClientForApi1()).createNewUser(userName: viewModel.username, password: viewModel.password, email: viewModel.email, onResultObtained: {
//                               (isSuccess,result) in
//                               
//                               let res=result as! NewUserRegistrationResponse
//                               if(!res.success)
//                               {
//                                   viewModel.toastMessage=res.message!
//                                   viewModel.showToast=true
//                               }
//                               else
//                               {
//                                   viewModel.toastMessage="Login successfull"
//                                   viewModel.showToast=true
//                                   viewModel.navigationPath.removeAll()
//                                   viewModel.navigationPath.append("aaaa")
//                               }
//                           }, completionHandler: {
//                               error in
//                           })
//                       }
//
//                       
//                       
//
//                   }, label: {
//                       Text("Submit").padding(.vertical,10).frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/)
//                   }).buttonStyle(.borderedProminent).padding(.top,40)
//                       .navigationDestination(for: String.self)
//                                   {
//                                       path in
//                                       ContentView(viewModel: ContentView.ViewModel())
//                                   }
//                                   .navigationBarBackButtonHidden(true)
//                   Text("create new account").onTapGesture {
//                       viewModel.isUserLoginView.toggle()
//                   }
//               }.padding(20).simpleToast(isPresented: $viewModel.showToast, options: toastOptions) {
//                   HStack()
//                   {
//                       Text(viewModel.toastMessage)
//                   }
//               }

            
        }
    }
}


#Preview {
    UserAuthentication(viewModel: UserAuthentication.ViewModel())

}

extension UserAuthentication {
    class ViewModel: ObservableObject {
        @Published var toastMessage = "Loading..."
        @Published var username = ""
        @Published var email = ""
        @Published var password = ""
        @Published var showToast=false
        @Published var isUserLoginView=true
        @Published var navigationPath = [String]()
        init() {
        
        }
    }
}

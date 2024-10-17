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
    
    
    var body: some View {
       
        NavigationStack
        {
            VStack
            {
                Text("Logo")
                    .frame(minHeight: 150)
                LoginView(viewModel: viewModel)
                .simpleToast(isPresented: $viewModel.showToast, options: toastOptions) {
                    HStack()
                    {
                        Text(viewModel.toastMessage)
                    }.padding(.top,100)
                }
                
                
            }
            .frame(maxWidth: .infinity,  maxHeight: .infinity,alignment: .bottom )
            .background(Color.theme.primary_color)
        }
        
    }
    
    struct LoginView:View {
        @State private var isLoggedIn: Bool = false
        @ObservedObject  var viewModel:UserAuthentication.ViewModel
        init(@ObservedObject viewModel:UserAuthentication.ViewModel )
        {
            self.viewModel=viewModel
        }
        var body: some View
        {
            VStack
            {
                Text("Welcome")
                    .padding(.vertical,20)
                    .frame(maxWidth: .infinity,alignment: .leading)
                    .font(.custom(Font.family.JURA_BOLD
                                  , size: Font.size.title_large_font_size))
                if(!viewModel.isUserLoginView)
                {
                    TextField("Email",text: $viewModel.email) .padding(15)
                        .background(Color.theme.textField_background)
                        .textInputAutocapitalization(.never)
                        .font(.custom(Font.family.IRINA_SANS_REGULAR,size: Font.size.body_font_size))
                    
                }
                TextField("Username",text: $viewModel.username)
                    .padding(15)
                    .textInputAutocapitalization(.never)
                    .background(Color.theme.textField_background)
                    .font(.custom(Font.family.IRINA_SANS_REGULAR,size: Font.size.body_font_size))
               HStack
                {
                    ZStack
                    {
                        if(viewModel.isSecured)
                        {
                            SecureField("Password",text: $viewModel.password)
                                .padding(15)
                                .textInputAutocapitalization(.never)
                                .keyboardType(.asciiCapable)
                                .background(Color.theme.textField_background)
                                .font(.custom(Font.family.IRINA_SANS_REGULAR,size: Font.size.body_font_size))
                        }
                    else
                        {
                        TextField("Password",text: $viewModel.password)
                            .padding(15)
                            .textInputAutocapitalization(.never)
                            .keyboardType(.asciiCapable)
                            .background(Color.theme.textField_background)
                            .font(.custom(Font.family.IRINA_SANS_REGULAR,size: Font.size.body_font_size))
                    }
                    }
                    Button(action: {
                        self.viewModel.isSecured.toggle()
                               }) {
                                   Image(systemName: self.viewModel.isSecured ? "eye.slash" : "eye")
                                       .accentColor(.gray)
                               }.padding(8)
                }.background(Color.theme.textField_background)
                Spacer()
                Button(action: {
                    isLoggedIn = true
                    callCorrespondingApiCall(viewModel: self.viewModel)
                }, label: {
                    Text("Login")
                        .frame(maxWidth: .infinity)
                        .padding(.vertical,10)
                        .foregroundColor(Color.theme.backgroundColor)
                        .background(Color.theme.primary_color.cornerRadius(60))
                })
                .padding(30)
                // Navigate to the next view based on the login state
                .navigationDestination(isPresented:$isLoggedIn) {
                  ContentView(viewModel: ContentView.ViewModel())
                        .navigationBarBackButtonHidden()
                }
                let buttonText:String =  if(viewModel.isUserLoginView)
                {
                    "Register a new account "
                }
                else
                { "Login with existing account"}
                
                Text(buttonText).font(.custom(Font.family.IRINA_SANS_REGULAR, size: Font.size.small))
                    .foregroundColor(Color.theme.accent_color)
                    .underline()
                    .onTapGesture {
                        viewModel.isUserLoginView.toggle()
                }
            }
            .padding(30)
            .frame(maxWidth: .infinity)
            .background(Color.theme.backgroundColor.cornerRadius(20).ignoresSafeArea(edges: .bottom))
        }
        
        func callCorrespondingApiCall(@ObservedObject viewModel:UserAuthentication.ViewModel)
        {
            
    //       if(viewModel.isUserLoginView)
//       {
//           ApiHandler(apiCallManager: Greeting().getHttpClientForApi1()).verifyUserDetails(userName:viewModel.username, password: viewModel.password) { (isSuccess, result) in
//
//               let res=result as! UserAuthenticationResponse
//               if(!res.success)
//               {
//                   viewModel.toastMessage=res.message!
//                   viewModel.showToast=true
//               }
//               else
//               {
//                   viewModel.toastMessage="Login successfull"
//                   viewModel.showToast=true
//                   viewModel.navigationPath.removeAll()
//                   viewModel.navigationPath.append("aaaa")
//               }
//
//
//           } completionHandler: { err in
//               print("cccc")
//           }
//       }else
//       {
//           ApiHandler(apiCallManager: Greeting().getHttpClientForApi1()).createNewUser(userName: viewModel.username, password: viewModel.password, email: viewModel.email, onResultObtained: {
//               (isSuccess,result) in
//
//               let res=result as! NewUserRegistrationResponse
//               if(!res.success)
//               {
//                   viewModel.toastMessage=res.message!
//                   viewModel.showToast=true
//               }
//               else
//               {
//                   viewModel.toastMessage="Login successfull"
//                   viewModel.showToast=true
//                   viewModel.navigationPath.removeAll()
//                   viewModel.navigationPath.append("aaaa")
//               }
//           }, completionHandler: {
//               error in
//           })
//       }
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
     
        @Published  var isSecured: Bool = true
        init() {
            
        }
    }
}

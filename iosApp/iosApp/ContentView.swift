import SwiftUI
import shared
struct ContentView: View {
    @ObservedObject private(set) var viewModel: ViewModel
  
    var body: some View {

     VStack(alignment: .center, spacing: 8)
        {
            
            
//            TextField("Username",text: $viewModel.username)  .padding(10)
//                .overlay(RoundedRectangle(cornerRadius: 4).stroke(Color.gray, lineWidth: 2))
//            
//            TextField("Password",text: $viewModel.password).padding(10)
//                .overlay(RoundedRectangle(cornerRadius: 4).stroke(Color.gray, lineWidth: 2))

            
            Text("Forgot Password").font(.custom("reset passord", size: 12)).frame(maxWidth: .infinity,alignment:.trailing)
           
            Button(action: {
                var inst: GGGGOnDOne{
                    class FFF:GGGGOnDOne
                    {
                        func aaaa() {
                          print("ffff")
                        }
                    }
                    return FFF()
                }
//                GGGG().cakk(a: "12", b: "12", c: 12, d: inst)
//                exampleFunction(param1: "asdasd", param2: "asdsa", ourFunc: { (no1, no2) -> Int in
//                    return no1 + no2
//                  })
                GGGG().aaa(c: {(a,d) -> Void in
                    print(a)
                    print(d)
                    if(a) as! Bool
                    {
                        print("asdasd")
                    }
                })
                ApiHandler(apiCallManager: Greeting().getHttpClientForApi1()).verifyUserDetails(userName: "aaa@aaa.com", password: "aaa", onResultObtained: {(a,b) in
                print(a)
                    print(b)
                }, completionHandler: {err in
                    print(err)
                })
            }, label: {
                Text("submit").padding(.vertical,10).frame(maxWidth: 12   )
            }).buttonStyle(.borderedProminent).padding(.top,40)
            
        }.padding(20)
       
    }
 
  
}


#Preview {
    ContentView(viewModel: ContentView.ViewModel())
}
extension ContentView {
    class ViewModel: ObservableObject {
        @Published var text = "Loading..."
        @Published var username = ""
        @Published var password = ""
        init() {
           
        }
    }
}

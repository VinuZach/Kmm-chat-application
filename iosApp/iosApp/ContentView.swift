import SwiftUI
import shared
struct ContentView: View {
    @ObservedObject private(set) var viewModel: ViewModel
  
    var body: some View {

     VStack(alignment: .center, spacing: 8)
        {
            
            
            TextField("Username",text: $viewModel.username)  .padding(10)
                .overlay(RoundedRectangle(cornerRadius: 4).stroke(Color.gray, lineWidth: 2))
            
            TextField("Password",text: $viewModel.password).padding(10)
                .overlay(RoundedRectangle(cornerRadius: 4).stroke(Color.gray, lineWidth: 2))

            
            Text("Forgot Password").font(.custom("reset passord", size: 12)).frame(maxWidth: .infinity,alignment:.trailing)
        
            Button(action: {
               
               
            }, label: {
                Text("Submit").padding(.vertical,10).frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/)
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

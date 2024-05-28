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
            Text("c")
            
        }.navigationBarBackButtonHidden(true)
       
    }
}


#Preview {
    ContentView(viewModel: ContentView.ViewModel())
}
extension ContentView {
    class ViewModel: ObservableObject {
        @Published var toastMessage = "Loading..."
        @Published var username = ""
        @Published var email = ""
        @Published var password = ""
        @Published var showToast=false
        @Published var isUserLoginView=true
        init() {
        
        }
    }
}

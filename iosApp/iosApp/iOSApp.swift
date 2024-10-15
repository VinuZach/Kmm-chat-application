import SwiftUI

@main
struct iOSApp: App {
	var body: some Scene {
        
		WindowGroup {
			
            //UserAuthentication(viewModel: UserAuthentication.ViewModel())
            ContentView(viewModel: ContentView.ViewModel())

		}
	}
}

//
//  Color.swift
//  iosApp
//
//  Created by Vinu on 13/10/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
 import SwiftUI

extension Color
{
    static let theme=ColorTheme()
}

struct ColorTheme
{
    let backgroundColor=Color("background_color")
    let accent_color=Color("accent_color")
    let primary_color=Color("primary_color")
    let secondary_color=Color("secondary_color")
    let textField_background=Color("textfield_background")
}

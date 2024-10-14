//
//  Font_Constant.swift
//  iosApp
//
//  Created by Vinu on 08/10/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import SwiftUI

extension Font
{
    static let family=FontFamily()
    static let size=FontSize()
}
struct FontFamily
{
    let JURA_BOLD="Jura-Bold"
    let IRINA_SANS_REGULAR="InriaSans-Regular"
}
struct FontSize
{
    let body_font_size:CGFloat=18
    let title_large_font_size:CGFloat=30
    let small:CGFloat=15
}



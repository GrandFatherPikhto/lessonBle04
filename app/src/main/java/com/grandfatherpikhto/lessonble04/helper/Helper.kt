package com.grandfatherpikhto.lessonble04.helper

import android.app.Activity
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment

typealias OnClickItemListener<T> = (T, View) -> Unit
typealias OnLongClickItemListener<T> = (T, View) -> Unit


fun linkMenu(menuHost: MenuHost, link: Boolean, menuProvider: MenuProvider) {
    if (link) {
        menuHost.addMenuProvider(menuProvider)
    } else {
        menuHost.removeMenuProvider(menuProvider)
    }
}

fun Activity.linkMenu(link: Boolean, menuProvider: MenuProvider)
    = linkMenu(this as MenuHost, link, menuProvider)

fun Fragment.linkMenu(link: Boolean, menuProvider: MenuProvider)
    = linkMenu(requireActivity() as androidx.core.view.MenuHost, link, menuProvider)
package com.heartsyncradio.db

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

fun createDatabase(context: Context): HrvXoDatabase {
    val driver = AndroidSqliteDriver(HrvXoDatabase.Schema, context, "hrvxo.db")
    return HrvXoDatabase(driver)
}

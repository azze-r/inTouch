package com.example.stayconnected.sample

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SavedDate (
    var value: Long = 0,
    @PrimaryKey var id: Long = 0
) :RealmObject()
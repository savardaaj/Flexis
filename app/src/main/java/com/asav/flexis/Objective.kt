package com.asav.flexis

import java.util.Date

class Objective {

    var id: String? = null
    var userId: String? = null
    var name: String? = null
    var description: String? = null
    var effort: String? = null
    var duration: String? = null
    var frequency: String? = null
    var status: String? = null
    var dateCompleted: Date? = null
    var timeblock: TimeBlock? = null
    var timeblockId: String? = null
    var isComplete: Boolean = false
}
package za.co.no9.mes.domain

import java.util.*


data class Event(val id: Int, val `when`: Date, val name: String, val content: String)

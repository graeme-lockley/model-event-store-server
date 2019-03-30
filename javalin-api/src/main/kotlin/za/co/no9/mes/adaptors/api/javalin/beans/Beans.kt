package za.co.no9.mes.adaptors.api.javalin.beans

import java.util.*


class Event(val id: Int, val `when`: Date, val name: String, val content: String)


class NewEvent(val name: String, val content: String)


class Topic (val id: Int, val name: String)


class NewTopic(val name: String)



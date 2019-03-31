package za.co.no9.mes.domain


sealed class Error


data class UnknownTopicID(
        val topicID: Int) : Error()


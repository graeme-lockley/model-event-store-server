package za.co.no9.mes.adaptors.api.javalin


internal class PostOffice(numberOfMailboxes: Int) {
    private val mailboxes: Array<Mailbox> =
            (1..numberOfMailboxes).map { Mailbox() }.toTypedArray()


    fun mailbox(id: String): Mailbox =
            mailboxes[Math.abs(id.hashCode()) % mailboxes.size]
}
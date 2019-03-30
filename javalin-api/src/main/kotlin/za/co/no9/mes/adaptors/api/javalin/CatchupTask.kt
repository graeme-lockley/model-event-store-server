package za.co.no9.mes.adaptors.api.javalin


internal class CatchupTask(private val session: Session) : MailboxTask {


    override fun process() {
        session.refresh()
    }
}

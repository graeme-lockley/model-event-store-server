package za.co.no9.ses8.adaptors.api.javalin;

class CatchupTask implements MailboxTask {
    private Session session;


    CatchupTask(Session session) {
        this.session = session;
    }


    @Override
    public void process() {
        session.refresh();
    }
}

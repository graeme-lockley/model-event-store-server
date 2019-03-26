package za.co.no9.ses8.adaptors.api.javalin;

class PostOffice {
    private Mailbox[] mailboxes;


    PostOffice(int numberOfMailboxes) {
        mailboxes = new Mailbox[numberOfMailboxes];

        for (int lp = 0; lp < numberOfMailboxes; lp += 1) {
            mailboxes[lp] = new Mailbox();
        }
    }


    Mailbox mailbox(String id) {
        int index =
                Math.abs(id.hashCode()) % mailboxes.length;

        return mailboxes[index];
    }
}
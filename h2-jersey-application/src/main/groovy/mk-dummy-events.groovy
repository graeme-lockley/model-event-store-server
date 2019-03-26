if (args.length == 0) {
    writeSQL(10)
} else {
    int total =
            Integer.parseInt(args[0])

    writeSQL(total)
}

static def writeSQL(int total) {
    for (int count = 0; count < total; count += 1) {
        switch (count % 3) {
            case 0:
                writeLine(
                        "ClientAdded",
                        "{name: \"" + randomString(1, 'A' as char, 'Z' as char) + randomString(10, 'a' as char, 'z' as char) + "\", customerID: " + count + "}")
                break

            case 1:
                writeLine(
                        "AccountAdded",
                        "{productID: \"CUR023\", numbers: \"" + randomString(10, '0' as char, '9' as char) + "\", customerID: " + count - 1 + "}")
                break

            case 2:
                writeLine(
                        "AccountAdded",
                        "{productID: \"CUR027\", numbers: \"" + randomString(10, '0' as char, '9' as char) + "\", customerID: " + count - 2 + "}")
                break
        }
    }
}


static def writeLine(eventName, content) {
    println("INSERT INTO PUBLIC.EVENT (WHEN, NAME, CONTENT) VALUES (now(), '" + eventName + "', '" + content + "');");
}


static def randomString(int stringLength, char leftLimit, char rightLimit) {
    Random random =
            new Random()

    StringBuilder buffer =
            new StringBuilder(stringLength)

    for (int i = 0; i < stringLength; i++) {
        char randomLimitedInt =
                leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1))

        buffer.append(randomLimitedInt)
    }

    return buffer.toString()
}

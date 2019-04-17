package com.eresearch.author.finder.connector.communicator;


import java.net.URI;

public interface Communicator {

    String communicateWithElsevier(URI uri);
}

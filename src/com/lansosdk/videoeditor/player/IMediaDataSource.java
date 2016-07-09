
package com.lansosdk.videoeditor.player;

import java.io.IOException;

@SuppressWarnings("RedundantThrows")
public interface IMediaDataSource {
    int	 readAt(long position, byte[] buffer, int offset, int size) throws IOException;

    long getSize() throws IOException;

    void close() throws IOException;
}

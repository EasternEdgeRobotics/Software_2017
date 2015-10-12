package com.easternedgerobotics.rov.event;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import rx.Observable;

import java.util.function.Consumer;

class UdpConnectionHandler implements ConnectionHandler<DatagramPacket, DatagramPacket> {
    private Consumer<byte[]> callback;

    public UdpConnectionHandler(Consumer<byte[]> callback) {
        this.callback = callback;
    }

    @Override
    public Observable<Void> handle(final ObservableConnection<DatagramPacket, DatagramPacket> inbound) {
        return inbound.getInput().flatMap(packet -> {
            final ByteBuf content = packet.content();
            final byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);
            callback.accept(bytes);

            return Observable.empty();
        });
    }
}

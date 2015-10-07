package com.easternedgerobotics.rov.event;

import com.easternedgerobotics.rov.event.io.Serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import rx.Observable;
import rx.subjects.Subject;

class UdpConnectionHandler implements ConnectionHandler<DatagramPacket, DatagramPacket> {
    private final Serializer serializer;

    private final Subject subject;

    UdpConnectionHandler(Serializer serializer, Subject subject) {
        this.serializer = serializer;
        this.subject = subject;
    }

    @Override
    public Observable<Void> handle(final ObservableConnection<DatagramPacket, DatagramPacket> inbound) {
        return inbound.getInput().flatMap(packet -> {
            final ByteBuf content = packet.content();
            final byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);
            subject.onNext(serializer.deserialize(bytes));

            return Observable.empty();
        });
    }
}

package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.event.EventPublisher;
import com.easternedgerobotics.rov.event.UdpEventPublisher;
import com.easternedgerobotics.rov.io.Joystick;
import com.easternedgerobotics.rov.io.Joysticks;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.pmw.tinylog.Logger;
import rx.Observable;

import java.util.concurrent.TimeUnit;

final class Topside extends Application {
    Button start;

    Button stop;

    private Topside() {

    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        primaryStage.setTitle("Master Start");
        start = new Button("Start");
        stop = new Button("Stop");
        start.setOnAction(e -> {
            Sliders.display("Controller Settings");
            Current.display("Current Sensors");
            Pressure.display("Pressure Sensors");
        });

        final int spacing = 5;
        final int width = 100;
        final int height = 100;
        final VBox buttons = new VBox();
        buttons.setPadding(new Insets(2, 2, 2, 2));
        buttons.setSpacing(spacing);
        buttons.getChildren().addAll(start, stop);

        final int minHeight = 85;
        final int minWidth = 150;
        final Scene window = new Scene(buttons, width, height);
        primaryStage.setMinHeight(minHeight);
        primaryStage.setMinWidth(minWidth);
        primaryStage.setScene(window);
        primaryStage.show();
    }

    private static final long HEARTBEAT_GAP = 100;

    public static void main(final String[] args) throws InterruptedException {
        final String app = "topside";
        final HelpFormatter formatter = new HelpFormatter();
        final Option broadcast = Option.builder("b")
            .longOpt("broadcast")
            .hasArg()
            .argName("ADDRESS")
            .desc("use ADDRESS to broadcast messages")
            .required()
            .build();

        final Options options = new Options();
        options.addOption(broadcast);

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine arguments = parser.parse(options, args);

            final EventPublisher eventPublisher = new UdpEventPublisher(arguments.getOptionValue("b"));
            final HeartbeatController heartbeatController = new HeartbeatController(
                eventPublisher, Observable.interval(HEARTBEAT_GAP, TimeUnit.MILLISECONDS));
            final Observable<Joystick> joystick = Joysticks.logitechExtreme3dPro();
            joystick.flatMap(Joystick::axes).subscribe(eventPublisher::emit, Logger::error);

            heartbeatController.start();

            Logger.info("Waiting");
            eventPublisher.await();
        } catch (final ParseException e) {
            formatter.printHelp(app, options, true);
            System.exit(1);
        }
    }
}

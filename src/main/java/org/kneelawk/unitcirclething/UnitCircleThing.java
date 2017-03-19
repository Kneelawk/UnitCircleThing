package org.kneelawk.unitcirclething;

import java.awt.image.BufferedImage;
import java.io.IOException;

import io.humble.video.Codec;
import io.humble.video.Encoder;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

public class UnitCircleThing {

	public static final int WIDTH = 1920;
	public static final int HEIGHT = 1080;

	public static void main(String[] args) {
		System.out.println("Setting up encoder...");

		Rational frametime = Rational.make(1, 60);

		Muxer muxer = Muxer.make("output.mkv", null, "mkv");
		MuxerFormat format = muxer.getFormat();
		Codec codec = Codec.findEncodingCodec(Codec.ID.CODEC_ID_VP9);

		Encoder encoder = Encoder.make(codec);
		encoder.setWidth(WIDTH);
		encoder.setHeight(HEIGHT);

		PixelFormat.Type pixelFormat = PixelFormat.Type.PIX_FMT_YUV420P;
		encoder.setPixelFormat(pixelFormat);
		encoder.setTimeBase(frametime);

		if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
			encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);

		encoder.open(null, null);
		muxer.addNewStream(encoder);

		try {
			muxer.open(null, null);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			return;
		}

		MediaPicture picture = MediaPicture.make(encoder.getWidth(), encoder.getHeight(), pixelFormat);
		picture.setTimeBase(frametime);
		MediaPictureConverter converter = MediaPictureConverterFactory
				.createConverter(MediaPictureConverterFactory.HUMBLE_BGR_24, picture);

		System.out.println("Writing video...");

		MediaPacket packet = MediaPacket.make();
		for (int i = 0; i < 5 * 60; i++) {
			BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
			generateImage(image, i);

			converter.toPicture(picture, image, i);

			do {
				encoder.encode(packet, picture);
				if (packet.isComplete())
					muxer.write(packet, false);
			} while (packet.isComplete());
		}

		do {
			encoder.encode(packet, null);
			if (packet.isComplete())
				muxer.write(packet, false);
		} while (packet.isComplete());

		muxer.close();
	}

	public static void generateImage(BufferedImage image, int i) {

	}
}

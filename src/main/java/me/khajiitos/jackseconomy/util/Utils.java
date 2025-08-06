package me.khajiitos.jackseconomy.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.math.BigDecimal;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

public class Utils {
    public static final Codec<BigDecimal> BIG_DECIMAL_CODEC = Codec.STRING.xmap(BigDecimal::new, BigDecimal::toPlainString);
    public static final StreamCodec<ByteBuf, BigDecimal> BIG_DECIMAL_STREAM_CODEC = ByteBufCodecs.fromCodec(BIG_DECIMAL_CODEC);

    public static final StreamCodec<ByteBuf, int[]> INT_ARRAY_STREAM_CODEC = StreamCodec.of(
            (buf, array) -> {
                buf.writeInt(array.length);
                for (int value : array) {
                    buf.writeInt(value);
                }
            },
            buf -> {
                int length = buf.readInt();
                int[] array = new int[length];
                for (int i = 0; i < length; i++) {
                    array[i] = buf.readInt();
                }
                return array;
            }
    );

    public static <T> StreamCodec<ByteBuf, T[]> arrayStreamCodec(StreamCodec<ByteBuf, T> codec, IntFunction<T[]> factory) {
        return StreamCodec.of(
                (buf, array) -> {
                    buf.writeInt(array.length);
                    for (T value : array) {
                        codec.encode(buf, value);
                    }
                },
                buf -> {
                    int length = buf.readInt();
                    T[] array = factory.apply(length);
                    for (int i = 0; i < length; i++) {
                        array[i] = codec.decode(buf);
                    }
                    return array;
                }
        );
    }

    public static float clamp(float value, float min, float max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }

        return value;
    }

    public static double clamp(double value, double min, double max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }

        return value;
    }

    public static int hexToMinecraftColor(String hexColor) {
        boolean includesHash = hexColor.charAt(0) == '#';
        if (includesHash) hexColor = hexColor.substring(1);

        int color = Integer.parseInt(hexColor, 16);

        if (hexColor.length() == 6) {
            color |= 0xFF000000;
        }

        return color;
    }
}

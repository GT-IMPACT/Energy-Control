package com.zuxelus.energycontrol.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketChat implements IMessage, IMessageHandler<PacketChat, IMessage> {
	private String message;
	private int type;
	private int value;

	public PacketChat() { }

	public PacketChat(String message, int type, int value) {
		this.message = message;
		this.type = type;
		this.value = value;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		message = ByteBufUtils.readUTF8String(buf);
		type = buf.readInt();
		value = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, message);
		buf.writeInt(type);
		buf.writeInt(value);
	}

	@Override
	public IMessage onMessage(PacketChat messages, MessageContext ctx) {
		switch (messages.type) {
		case 1:
			String[] modeName = { I18n.format("info.normal"), I18n.format("info.rapidfire"), I18n.format("info.spread"),
					I18n.format("info.sniper"), I18n.format("info.flame"), I18n.format("info.explosive") };
			Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentTranslation(messages.message, modeName[messages.value]), false);
			break;
		default:
			Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentTranslation(messages.message), false);
			break;
		}
		return null;
	}
}

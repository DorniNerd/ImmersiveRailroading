package cam72cam.immersiverailroading.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class SyncdTileEntity extends TileEntity {
	public boolean hasTileData;
	
	public boolean isLoaded() {
		return this.hasWorld() && (!world.isRemote || hasTileData);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (!world.isRemote) {
			world.notifyBlockUpdate(getPos(), world.getBlockState(getPos()), world.getBlockState(getPos()), 1 + 2 + 8);
			world.notifyNeighborsOfStateChange(pos, this.getBlockType(), true);
		}
	}
	
	public void writeUpdateNBT(NBTTagCompound nbt) {
	}
	public void readUpdateNBT(NBTTagCompound nbt) {
	}
	
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		this.writeUpdateNBT(nbt);
		
		return new SPacketUpdateTileEntity(this.getPos(), 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
		this.readUpdateNBT(pkt.getNbtCompound());
		super.onDataPacket(net, pkt);
		if (updateRerender()) {
			world.markBlockRangeForRenderUpdate(getPos(), getPos());
		}
		hasTileData = true;
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		this.writeToNBT(tag);
		this.writeUpdateNBT(tag);
		return tag;
	}
	
	public boolean updateRerender() {
		return false;
	}
	
	@Override 
	public void handleUpdateTag(NBTTagCompound tag) {
		this.readFromNBT(tag);
		this.readUpdateNBT(tag);
		super.handleUpdateTag(tag);
		if (updateRerender()) {
			world.markBlockRangeForRenderUpdate(getPos(), getPos());
		}
		hasTileData = true;
	}
}

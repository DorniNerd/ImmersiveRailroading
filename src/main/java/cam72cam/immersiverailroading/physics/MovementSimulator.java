package cam72cam.immersiverailroading.physics;

import cam72cam.immersiverailroading.util.Speed;
import cam72cam.immersiverailroading.util.VecUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import trackapi.lib.ITrackTile;
import trackapi.lib.Util;

public class MovementSimulator {
	private World world;
	private TickPos position;
	private float bogeyFrontOffset;
	private float bogeyRearOffset;
	private double gauge;

	public MovementSimulator(World world, TickPos startPos, float bogeyFrontOffset, float bogeyRearOffset, double gauge) {
		this.world = world;
		this.position = startPos.clone(); //  We may modify it for convenience later on
		this.bogeyFrontOffset = bogeyFrontOffset;
		this.bogeyRearOffset = bogeyRearOffset;
		this.gauge = gauge;
	}
	
	public TickPos nextPosition(double moveDistance) {
		position.tickID += 1;
		position.isOffTrack = false;
		TickPos origPosition = position.clone();

		if (Math.abs(moveDistance) < 0.001) {
			position.speed = Speed.ZERO;
			return position;
		}
		
		position.speed = Speed.fromMinecraft(moveDistance);

		boolean isReverse = moveDistance < 0;

		Vec3d front = frontBogeyPosition();
		Vec3d rear = rearBogeyPosition();
		
		if (isReverse) {
			moveDistance = -moveDistance;
			position.frontYaw += 180;
			position.rearYaw += 180;
			position.rotationYaw += 180;
			//position.rotationPitch = -position.rotationPitch;
			position.rotationYaw = (position.rotationYaw + 360f) % 360f;
			position.frontYaw = (position.frontYaw + 360f) % 360f;
			position.rearYaw = (position.rearYaw + 360f) % 360f;
		}

		
		
		Vec3d nextFront = nextPosition(front, position.rotationYaw, position.frontYaw, moveDistance);
		Vec3d nextRear = nextPosition(rear, position.rotationYaw, position.rearYaw, moveDistance);
		if (nextFront.equals(front) || nextRear == rear) {
			origPosition.speed = Speed.ZERO;
			if (position.isOffTrack) {
				origPosition.isOffTrack = true;
			}
			return origPosition;
		}
		Vec3d frontDelta = front.subtractReverse(nextFront);
		Vec3d rearDelta = rear.subtractReverse(nextRear);
		position.frontYaw = VecUtil.toYaw(frontDelta);
		position.rearYaw = VecUtil.toYaw(rearDelta);

		Vec3d currCenter = VecUtil.between(front, rear);
		Vec3d nextCenter = VecUtil.between(nextFront, nextRear);
		Vec3d deltaCenter = currCenter.subtractReverse(nextCenter);

		Vec3d bogeySkew = nextRear.subtractReverse(nextFront);
		position.rotationYaw = VecUtil.toYaw(bogeySkew);
		position.rotationPitch = (float) Math.toDegrees(MathHelper.atan2(bogeySkew.y, nextRear.distanceTo(nextFront)));

		if (isReverse) {
			position.frontYaw += 180;
			position.rearYaw += 180;
			//rotationYaw += 180;
			//position.rotationPitch = -position.rotationPitch;
			position.rotationYaw = (position.rotationYaw + 360f) % 360f;
			position.frontYaw = (position.frontYaw + 360f) % 360f;
			position.rearYaw = (position.rearYaw + 360f) % 360f;
		}
		
		

		position.position = position.position.add(deltaCenter);
		if (world.isAirBlock(new BlockPos(position.position))) {
			// Fall
			position.position = position.position.addVector(0, -0.1, 0);
		}
		
		return position;
	}
	
	private ITrackTile findTrack(Vec3d currentPosition, float trainYaw) {
		ITrackTile te = Util.getTileEntity(world, currentPosition, true);
		if (te != null && te.getTrackGauge() == gauge) {
			return te;
		}
		te = Util.getTileEntity(world, currentPosition.add(VecUtil.fromYaw(-1, trainYaw)), true);
		if (te != null && te.getTrackGauge() == gauge) {
			return te;
		}
		te = Util.getTileEntity(world, currentPosition.add(VecUtil.fromYaw(1, trainYaw)), true);
		if (te != null && te.getTrackGauge() == gauge) {
			return te;
		}
		return null;
	}
	

	public Vec3d nextPosition(Vec3d currentPosition, float rotationYaw, float bogeyYaw, double distance) {
		ITrackTile rail = findTrack(currentPosition, rotationYaw);
		if (rail == null) {
			position.isOffTrack = true;
			return currentPosition;
		}
		// Not using bogey yaw here, is that OK?
		Vec3d result = rail.getNextPosition(currentPosition, VecUtil.fromYaw(distance, rotationYaw));
		if (result == null) {
			position.isOffTrack = true;
			return currentPosition;
		}
		return result;
	}

	public Vec3d frontBogeyPosition() {
		return VecUtil.fromYawPitch(bogeyFrontOffset, position.rotationYaw, position.rotationPitch).add(position.position);
	}

	public Vec3d rearBogeyPosition() {
		return VecUtil.fromYawPitch(bogeyRearOffset, position.rotationYaw, position.rotationPitch).add(position.position);
	}
}

package cam72cam.immersiverailroading.entity;


import cam72cam.immersiverailroading.library.GuiTypes;
import cam72cam.immersiverailroading.library.KeyTypes;
import cam72cam.immersiverailroading.registry.LocomotiveDefinition;
import cam72cam.immersiverailroading.util.Speed;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public abstract class Locomotive extends FreightTank {
	//private MovingSoundRollingStock hornSound;
	//private MovingSoundRollingStock idleSound;
	//protected MovingSoundRollingStock runSound;

	private static DataParameter<Float> THROTTLE = EntityDataManager.createKey(Locomotive.class, DataSerializers.FLOAT);
	private static DataParameter<Float> AIR_BRAKE = EntityDataManager.createKey(Locomotive.class, DataSerializers.FLOAT);
	

	private static final float throttleNotch = 0.04f;
	private static final float airBrakeNotch = 0.04f;


	public Locomotive(World world, String defID) {
		super(world, defID);

		this.getDataManager().register(THROTTLE, 0f);
		this.getDataManager().register(AIR_BRAKE, 0f);

		// hornSound = EnumSounds.get(this.getClass()).getHorn(this);
		// idleSound = EnumSounds.get(this.getClass()).getIdle(this);
		// runSound = EnumSounds.get(this.getClass()).getRun(this);

		this.entityCollisionReduction = 0.99F;
	}
	
	/*
	 * 
	 * Stock Definitions
	 * 
	 */
	
	public LocomotiveDefinition getDefinition() {
		return super.getDefinition(LocomotiveDefinition.class);
	}

	/*
	 * 
	 * EntityRollingStock Overrides
	 */

	@Override
	public GuiTypes guiType() {
		return null;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("throttle", getThrottle());
		nbttagcompound.setFloat("brake", getAirBrake());
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		setThrottle(nbttagcompound.getFloat("throttle"));
		setAirBrake(nbttagcompound.getFloat("brake"));
	}

	@Override
	public void setDead() {
		super.setDead();
		//System.out.println("Stopping audio");
		//Minecraft.getMinecraft().getSoundHandler().stopSound(idleSound);
		//Minecraft.getMinecraft().getSoundHandler().stopSound(runSound);
	}
	
	@Override
	public void handleKeyPress(Entity source, KeyTypes key) {
		switch(key) {
		case THROTTLE_UP:
			if (getThrottle() < 1) {
				setThrottle(getThrottle() + throttleNotch);
			}
			break;
		case THROTTLE_ZERO:
			setThrottle(0f);
			break;
		case THROTTLE_DOWN:
			if (getThrottle() > -1) {
				setThrottle(getThrottle() - throttleNotch);
			}
			break;
		case AIR_BRAKE_UP:
			if (getAirBrake() < 1) {
				setAirBrake(getAirBrake() + airBrakeNotch);
			}
			break;
		case AIR_BRAKE_ZERO:
			setAirBrake(0f);
			break;
		case AIR_BRAKE_DOWN:
			if (getAirBrake() > 0) {
				setAirBrake(getAirBrake() - airBrakeNotch);
			}
			break;
		default:
			super.handleKeyPress(source, key);
			break;
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		// runSound.setVolume(getSpeed().minecraft() > 0 ? 1 : 0);
		// idleSound.setVolume(getSpeed().minecraft() > 0 ? 0 : 1);
		
		simulateWheelSlip();
	}
	
	protected abstract int getAvailableHP();
	
	private double getAppliedTractiveEffort(Speed speed) {
		double locoEfficiency = 0.7f; //TODO config
		double outputHorsepower = Math.abs(Math.pow(getThrottle(), 2) * getAvailableHP());
		
		double tractiveEffortNewtons = (2650.0 * ((locoEfficiency * outputHorsepower) / Math.max(0.0001, Math.abs(speed.metric()))));
		return tractiveEffortNewtons;
	}
	
	private void simulateWheelSlip() {
		double applied = getAppliedTractiveEffort(this.getCurrentSpeed());
		double actual = this.getDefinition().getStartingTractionNewtons(gauge);
		if (applied > actual) {
			double speedMultiplier = 1;//Math.min(1, Math.abs(this.getCurrentSpeed().metric() * Math.abs(this.getThrottle()) * 2));//Hack for starting
			this.distanceTraveled += Math.copySign(Math.min((applied / actual - 1)/100, 0.8), getThrottle()) * speedMultiplier; //Wheel Slip
		}
	}
	
	public double getTractiveEffortNewtons(Speed speed) {	
		if (!this.isBuilt()) {
			return 0;
		}
		
		double tractiveEffortNewtons = getAppliedTractiveEffort(speed);
		
		
		if (tractiveEffortNewtons > this.getDefinition().getStartingTractionNewtons(gauge)) {
			// CRC Handbook of Physical Quantities. Boca Raton, FL: CRC Press, 1997: 145-156.
			double us = 0.74;
			double uk = 0.57;
			tractiveEffortNewtons = this.getDefinition().getStartingTractionNewtons(gauge) * (uk/us);
		}
		
		if (Math.abs(speed.minecraft()) > this.getDefinition().getMaxSpeed(gauge).minecraft()) {
			tractiveEffortNewtons = 0;
		}
		
		return Math.copySign(tractiveEffortNewtons, getThrottle());
	}

	/*
	 * 
	 * Misc Helper functions
	 */

	public void soundHorn() {
		//if (!Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(hornSound)) {
		//	Minecraft.getMinecraft().getSoundHandler().playSound(hornSound);
		//}
	}
	
	public float getThrottle() {
		return dataManager.get(THROTTLE);
	}
	public void setThrottle(float newThrottle) {
		if (this.getThrottle() != newThrottle) {
			dataManager.set(THROTTLE, newThrottle);
			triggerResimulate();
		}
	}
	
	public float getAirBrake() {
		return dataManager.get(AIR_BRAKE);
	}
	public void setAirBrake(float newAirBrake) {
		if (this.getAirBrake() != newAirBrake) {
			dataManager.set(AIR_BRAKE, newAirBrake);
			triggerResimulate();
		}
	}
}
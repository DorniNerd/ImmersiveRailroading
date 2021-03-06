package cam72cam.immersiverailroading.library;

import cam72cam.immersiverailroading.util.TextUtil;
import net.minecraft.util.text.TextComponentTranslation;

public enum ChatText {
	STOCK_BUILT("stock.built"), 
	STOCK_DISSASEMBLED("stock.dissasembled"), 
	STOCK_MISSING("stock.missing"),
	STOCK_INVALID("stock.invalid"),
	STOCK_WRONG_GAUGE("stock.wrong_gauge"),
	COUPLER_FRONT("coupler.front"),
	COUPLER_BACK("coupler.back"),
	COUPLER_ENGAGED("coupler.engaged"),
	COUPLER_DISENGAGED("coupler.disengaged"),
	COUPLER_STATUS_COUPLED("coupler.status.coupled"),
	COUPLER_STATUS_DECOUPLED_ENGAGED("coupler.status.decoupled.engaged"),
	COUPLER_STATUS_DECOUPLED_DISENGAGED("coupler.status.decoupled.disengaged"),
	BUILD_MISSING_TIES("build.missing.ties"),
	BUILD_MISSING_RAILS("build.missing.rails"),
	BUILD_MISSING_RAIL_BED("build.missing.rail_bed"),
	BUILD_MISSING_RAIL_BED_FILL("build.missing.rail_bed_fill"),
	SET_AUGMENT_FILTER("augment.set"),
	RESET_AUGMENT_FILTER("augment.reset"),
	;
	
	private String value;
	ChatText(String value) {
		this.value = value;
	}
	
	public String getRaw() {
		return "chat.immersiverailroading:" + value;
	}
	
	public TextComponentTranslation getMessage(Object... objects) {
		return new TextComponentTranslation(getRaw(), objects);
	}

	@Override
	public String toString() {
		return TextUtil.translate(getRaw());
	}
}

package ru.bclib.blockentities;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import ru.bclib.registry.BaseBlockEntities;

public class BaseSignBlockEntity extends SignBlockEntity {
	public BaseSignBlockEntity() {
		super();
	}

	@Override
	public BlockEntityType<?> getType() {
		return BaseBlockEntities.SIGN;
	}
}
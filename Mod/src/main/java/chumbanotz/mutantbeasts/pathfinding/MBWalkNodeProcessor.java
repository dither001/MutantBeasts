package chumbanotz.mutantbeasts.pathfinding;

import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.block.EndRodBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.block.MagmaBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.WebBlock;
import net.minecraft.block.WitherRoseBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.IFluidState;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class MBWalkNodeProcessor extends WalkNodeProcessor // MC-96319
{
	@Override
	protected PathNodeType func_215744_a(IBlockReader blockaccessIn, boolean canOpenDoorsIn, boolean canEnterDoorsIn, BlockPos pos, PathNodeType nodeType) {
		if (nodeType == PathNodeType.DOOR_WOOD_CLOSED && canOpenDoorsIn && canEnterDoorsIn) {
			nodeType = PathNodeType.WALKABLE;
		}

		if (nodeType == PathNodeType.DOOR_OPEN && (!canEnterDoorsIn || this.currentEntity.getWidth() > 0.85F)) {
			nodeType = PathNodeType.BLOCKED;
		}

		if (nodeType == PathNodeType.RAIL && !(blockaccessIn.getBlockState(pos).getBlock() instanceof AbstractRailBlock) && !(blockaccessIn.getBlockState(pos.down()).getBlock() instanceof AbstractRailBlock)) {
			nodeType = PathNodeType.FENCE;
		}

		if (nodeType == PathNodeType.LEAVES) {
			nodeType = this.currentEntity.getPathPriority(PathNodeType.LEAVES) == 0.0F ? PathNodeType.OPEN : PathNodeType.BLOCKED;
		}

		return nodeType;
	}

	@Override
	public PathNodeType checkNeighborBlocks(IBlockReader blockaccessIn, int x, int y, int z, PathNodeType nodeType) {
		if (nodeType == PathNodeType.WALKABLE) {
			try (BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain()) {
				for (int i = -1; i <= 1; ++i) {
					for (int j = -1; j <= 1; ++j) {
						if (i != 0 || j != 0) {
							BlockState state = blockaccessIn.getBlockState(blockpos$pooledmutableblockpos.setPos(i + x, y, j + z));
							Block block = state.getBlock();
							PathNodeType forgetype = block.getAiPathNodeType(state, blockaccessIn, blockpos$pooledmutableblockpos, this.currentEntity);
							if (forgetype == PathNodeType.DAMAGE_CACTUS || block instanceof CactusBlock) {
								nodeType = PathNodeType.DANGER_CACTUS;
							} else if (forgetype == PathNodeType.DAMAGE_FIRE || block instanceof FireBlock || block instanceof CampfireBlock && state.get(CampfireBlock.LIT)) {
								nodeType = PathNodeType.DANGER_FIRE;
							} else if (state.getFluidState().isTagged(FluidTags.LAVA)) {
								nodeType = PathNodeType.LAVA;
							} else if (forgetype == PathNodeType.DANGER_OTHER || block instanceof SweetBerryBushBlock || block instanceof WitherRoseBlock || block instanceof EndPortalBlock || block instanceof AbstractPressurePlateBlock || block instanceof WebBlock) {
								nodeType = PathNodeType.DANGER_OTHER;
							}
						}
					}
				}
			}
		}

		return nodeType;
	}

	@Override
	public PathNodeType getPathNodeType(IBlockReader blockaccessIn, int x, int y, int z) {
		PathNodeType pathnodeabove = this.getPathNodeTypeRaw(blockaccessIn, x, y, z);
		if (pathnodeabove == PathNodeType.OPEN && y >= 1) {
			BlockState blockstate = blockaccessIn.getBlockState(new BlockPos(x, y - 1, z));
			Block block = blockstate.getBlock();
			PathNodeType pathnodebelow = this.getPathNodeTypeRaw(blockaccessIn, x, y - 1, z);
			pathnodeabove = pathnodebelow != PathNodeType.WALKABLE && pathnodebelow != PathNodeType.OPEN && pathnodebelow != PathNodeType.WATER && pathnodebelow != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;

			if (pathnodebelow == PathNodeType.DAMAGE_FIRE) {
				pathnodeabove = PathNodeType.DAMAGE_FIRE;
			}

			if (pathnodebelow == PathNodeType.DAMAGE_CACTUS) {
				pathnodeabove = PathNodeType.DAMAGE_CACTUS;
			}

			if (pathnodebelow == PathNodeType.DAMAGE_OTHER) {
				pathnodeabove = PathNodeType.DAMAGE_OTHER;
			}

			if (pathnodebelow == PathNodeType.DANGER_OTHER) {
				pathnodeabove = PathNodeType.DANGER_OTHER;
			}

			if (block instanceof TrapDoorBlock && blockstate.get(TrapDoorBlock.OPEN)) {
				pathnodeabove = PathNodeType.BLOCKED;
			}
		}

		pathnodeabove = this.checkNeighborBlocks(blockaccessIn, x, y, z, pathnodeabove);
		return pathnodeabove;
	}

	@Override
	protected PathNodeType getPathNodeTypeRaw(IBlockReader blockaccessIn, int x, int y, int z) {
		BlockPos blockpos = new BlockPos(x, y, z);
		BlockState blockstate = blockaccessIn.getBlockState(blockpos);
		PathNodeType forgetype = blockstate.getAiPathNodeType(blockaccessIn, blockpos, this.currentEntity);
		if (forgetype != null)
			return forgetype;
		Block block = blockstate.getBlock();
		Material material = blockstate.getMaterial();
		if (blockstate.isAir(blockaccessIn, blockpos)) {
			return PathNodeType.OPEN;
		} else if (!(block instanceof TrapDoorBlock) && !(block instanceof LilyPadBlock)) {
			if (block instanceof FireBlock || block instanceof MagmaBlock || block instanceof CampfireBlock && blockstate.get(CampfireBlock.LIT)) {
				return PathNodeType.DAMAGE_FIRE;
			} else if (block instanceof CactusBlock) {
				return PathNodeType.DAMAGE_CACTUS;
			} else if (block instanceof SweetBerryBushBlock || block instanceof WitherRoseBlock || block instanceof EndPortalBlock || block instanceof NetherPortalBlock) {
				return PathNodeType.DAMAGE_OTHER;
			} else if (block instanceof WebBlock || block instanceof AbstractPressurePlateBlock || block instanceof SoulSandBlock) {
				return PathNodeType.DANGER_OTHER;
			} else if (block instanceof DoorBlock && material == Material.WOOD && !blockstate.get(DoorBlock.OPEN)) {
				return PathNodeType.DOOR_WOOD_CLOSED;
			} else if (block instanceof DoorBlock && material == Material.IRON && !blockstate.get(DoorBlock.OPEN)) {
				return PathNodeType.DOOR_IRON_CLOSED;
			} else if (block instanceof DoorBlock && blockstate.get(DoorBlock.OPEN)) {
				return PathNodeType.DOOR_OPEN;
			} else if (block instanceof AbstractRailBlock) {
				return PathNodeType.RAIL;
			} else if (block instanceof LeavesBlock || block instanceof AbstractGlassBlock) {
				return PathNodeType.LEAVES;
			} else if (!block.isIn(BlockTags.FENCES) && !block.isIn(BlockTags.WALLS) && !block.isIn(BlockTags.FLOWER_POTS) && (!(block instanceof FenceGateBlock) || blockstate.get(FenceGateBlock.OPEN)) && !(block instanceof EndRodBlock) && !(block instanceof AbstractSkullBlock)) {
				IFluidState ifluidstate = blockaccessIn.getFluidState(blockpos);
				if (ifluidstate.isTagged(FluidTags.WATER)) {
					return PathNodeType.WATER;
				} else if (ifluidstate.isTagged(FluidTags.LAVA)) {
					return PathNodeType.LAVA;
				} else {
					return blockstate.allowsMovement(blockaccessIn, blockpos, PathType.LAND) ? PathNodeType.OPEN : PathNodeType.BLOCKED;
				}
			} else {
				return PathNodeType.FENCE;
			}
		} else {
			return PathNodeType.TRAPDOOR;
		}
	}
}
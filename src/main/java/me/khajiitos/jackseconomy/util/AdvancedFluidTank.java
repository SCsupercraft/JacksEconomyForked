package me.khajiitos.jackseconomy.util;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An advanced version of the fluid tank
 */
public class AdvancedFluidTank extends FluidTank {
    /**
     * Can fluid can be added to the tank.
     */
    protected boolean allowInput = true;
    /**
     * Can fluid be drained from the tank.
     */
    protected boolean allowOutput = true;

    /**
     * Creates an advanced fluid tank, without constraints.
     *
     * @param capacity The capacity of this tank.
     */
    public AdvancedFluidTank(int capacity) {
        super(capacity);
    }
    /**
     * Creates an advanced fluid tank, with the specified constraints.
     *
     * @param capacity The capacity of this tank.
     * @param allowInput Can fluid can be added to the tank.
     * @param allowOutput Can fluid be drained from the tank.
     */
    public AdvancedFluidTank(int capacity, boolean allowInput, boolean allowOutput) {
        super(capacity);
        this.allowInput = allowInput;
        this.allowOutput = allowOutput;
    }
    /**
     * Creates an advanced fluid tank, with the specified constraints.
     *
     * @param capacity The capacity of this tank.
     * @param validator The function that checks if a fluid is valid.
     */
    public AdvancedFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }
    /**
     * Creates an advanced fluid tank, with the specified constraints and a validator.
     *
     * @param capacity The capacity of this tank.
     * @param allowInput Can fluid can be added to the tank.
     * @param allowOutput Can fluid be drained from the tank.
     * @param validator The function that checks if a fluid is valid.
     */
    public AdvancedFluidTank(int capacity, boolean allowInput, boolean allowOutput, Predicate<FluidStack> validator) {
        super(capacity, validator);
        this.allowInput = allowInput;
        this.allowOutput = allowOutput;
    }

    /**
     * @return FluidStack representing the fluid in the tank.
     */
    @Override
    public @NotNull FluidStack getFluid() {
        return super.getFluid().copy();
    }

    /**
     * @param resource FluidStack representing the Fluid and maximum amount of fluid to be filled.
     * @param action If SIMULATE, fill will only be simulated.
     * @return Amount of resource that was (or would have been, if simulated) filled.
     */
    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        if (!allowInput) {
            return 0;
        }
        return super.fill(resource, action);
    }

    /**
     * @param maxDrain Maximum amount of fluid to drain.
     * @param action If SIMULATE, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if simulated) drained.
     */
    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        if (!this.isFluidValid(this.fluid) || !allowOutput)
            return FluidStack.EMPTY;

        return super.drain(maxDrain, action);
    }

    /**
     * @implNote This implementation follows the constraints of both the supplied tank and its own constraints
     */
    public static class SuppliedFluidTank implements IFluidHandler {
        private Supplier<AdvancedFluidTank> fluidTankSupplier;
        private Predicate<FluidStack> validator;
        protected boolean allowInput;
        protected boolean allowOutput;

        /**
         * Creates a supplied fluid tank, using the same constraints as the supplied tank.
         *
         * @implNote This implementation follows the constraints of both the supplied tank and its own constraints
         * @param fluidTankSupplier The function that supplies the fluid tank.
         */
        public SuppliedFluidTank(Supplier<AdvancedFluidTank> fluidTankSupplier) {
            setSupplier(fluidTankSupplier);

            if (getTank() != null) {
                AdvancedFluidTank tank = getTank();
                allowInput = tank.allowInput;
                allowOutput = tank.allowOutput;
            } else throw new IllegalStateException();
        }

        /**
         * Creates a supplied fluid tank, using the specified constraints.
         *
         * @implNote This implementation follows the constraints of both the supplied tank and its own constraints
         * @param fluidTankSupplier The function that supplies the fluid tank.
         * @param allowInput Can fluid can be added to the tank.
         * @param allowOutput Can fluid be drained from the tank.
         */
        public SuppliedFluidTank(Supplier<AdvancedFluidTank> fluidTankSupplier, boolean allowInput, boolean allowOutput) {
            setSupplier(fluidTankSupplier);
            this.allowInput = allowInput;
            this.allowOutput = allowOutput;

            if (getTank() == null)
                throw new IllegalStateException();
        }

        /**
         * Updates this tank's supplier.
         *
         * @param fluidTankSupplier The function that supplies the fluid tank.
         * @return this.
         */
        public SuppliedFluidTank setSupplier(Supplier<AdvancedFluidTank> fluidTankSupplier) {
            this.fluidTankSupplier = fluidTankSupplier;
            return this;
        }

        /**
         * Updates this tank's validator.
         *
         * @param validator The function that checks if a fluid is valid.
         * @return this.
         */
        public SuppliedFluidTank setValidator(Predicate<FluidStack> validator) {
            this.validator = validator;
            return this;
        }

        /**
         * Returns the advanced fluid tank returned by the supplier.
         *
         * @return The advanced fluid tank supplied to this fluid handler.
         */
        public AdvancedFluidTank getTank() {
            return this.fluidTankSupplier.get();
        }

        /**
         * Returns the number of fluid storage units ("tanks") available.
         *
         * @return The number of tanks available.
         */
        @Override
        public int getTanks() {
            return getTank().getTanks();
        }

        /**
         * Returns the FluidStack in a given tank.
         *
         * <p>
         * <strong>IMPORTANT:</strong> This FluidStack <em>MUST NOT</em> be modified. This method is not for
         * altering internal contents. Any implementers who are able to detect modification via this method
         * should throw an exception. It is ENTIRELY reasonable and likely that the stack returned here will be a copy.
         * </p>
         *
         * <p>
         * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED FLUIDSTACK</em></strong>
         * </p>
         *
         * @param tank Tank to query.
         * @return FluidStack in a given tank. FluidStack.EMPTY if the tank is empty.
         */
        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return getTank().getFluidInTank(tank);
        }

        /**
         * Retrieves the maximum fluid amount for a given tank.
         *
         * @param tank Tank to query.
         * @return The maximum fluid amount held by the tank.
         */
        @Override
        public int getTankCapacity(int tank) {
            return getTank().getTankCapacity(tank);
        }

        /**
         * This function is a way to determine which fluids can exist inside a given handler. General purpose tanks will
         * basically always return TRUE for this.
         *
         * @param tank  Tank to query for validity
         * @param stack Stack to test with for validity
         * @return TRUE if the tank can hold the FluidStack, not considering current state.
         * (Basically, is a given fluid EVER allowed in this tank?) Return FALSE if the answer to that question is 'no.'
         */
        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            boolean valid = getTank().isFluidValid(tank, stack);
            return validator != null ? validator.test(stack) && valid : valid;
        }

        /**
         * Fills fluid into internal tanks, distribution is left entirely to the IFluidHandler.
         *
         * @param resource FluidStack representing the Fluid and maximum amount of fluid to be filled.
         * @param action   If SIMULATE, fill will only be simulated.
         * @return Amount of resource that was (or would have been, if simulated) filled.
         */
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!allowInput || !isFluidValid(0, resource))
                return 0;

            return getTank().fill(resource, action);
        }

        /**
         * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
         *
         * @param resource FluidStack representing the Fluid and maximum amount of fluid to be drained.
         * @param action   If SIMULATE, drain will only be simulated.
         * @return FluidStack representing the Fluid and amount that was (or would have been, if
         * simulated) drained.
         */
        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (!allowOutput || !isFluidValid(0, resource))
                return FluidStack.EMPTY;

            return getTank().drain(resource, action);
        }

        /**
         * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
         * <p>
         * This method is not Fluid-sensitive.
         *
         * @param maxDrain Maximum amount of fluid to drain.
         * @param action   If SIMULATE, drain will only be simulated.
         * @return FluidStack representing the Fluid and amount that was (or would have been, if
         * simulated) drained.
         */
        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (!allowOutput || !isFluidValid(0, getFluidInTank(0)))
                return FluidStack.EMPTY;

            return getTank().drain(maxDrain, action);
        }
    }
}

/*
 * Minecraft Forge - Forge Development LLC
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class RegistryObject<T> implements Supplier<T>
{
    private final ResourceLocation name;
    private final boolean optionalRegistry;
    @Nullable
    private T value;

    /**
     * @deprecated The uniqueness of registry super types will not be guaranteed starting in 1.19.
     * Use {@link #create(ResourceLocation, ResourceLocation, String)}.
     */
    @Deprecated(forRemoval = true, since = "1.18.2")
    public static <T extends IForgeRegistryEntry<T>, U extends T> RegistryObject<U> of(final ResourceLocation name, Supplier<Class<? super T>> registryType) {
        return new RegistryObject<>(name, registryType);
    }

    /**
     * Factory for a {@link RegistryObject} that stores the value of an object from the provided forge registry once it is ready.
     *
     * @param name the name of the object to look up in the forge registry
     * @param registry the forge registry
     * @return a {@link RegistryObject} that stores the value of an object from the provided forge registry once it is ready
     * @deprecated Use {@link #create(ResourceLocation, IForgeRegistry)} instead.
     */
    @Deprecated(forRemoval = true, since = "1.18.2")
    public static <T extends IForgeRegistryEntry<T>, U extends T> RegistryObject<U> of(final ResourceLocation name, IForgeRegistry<T> registry)
    {
        return new RegistryObject<>(name, registry);
    }

    /**
     * Factory for a {@link RegistryObject} that stores the value of an object from the provided forge registry once it is ready.
     *
     * @param name the name of the object to look up in the forge registry
     * @param registry the forge registry
     * @return a {@link RegistryObject} that stores the value of an object from the provided forge registry once it is ready
     */
    public static <T extends IForgeRegistryEntry<T>, U extends T> RegistryObject<U> create(final ResourceLocation name, IForgeRegistry<T> registry)
    {
        return new RegistryObject<>(name, registry);
    }

    /**
     * @deprecated The uniqueness of registry super types will not be guaranteed starting in 1.19.
     * Use {@link #create(ResourceLocation, ResourceLocation, String)}.
     */
    @Deprecated(forRemoval = true, since = "1.18.2")
    public static <T extends IForgeRegistryEntry<T>, U extends T> RegistryObject<U> of(final ResourceLocation name, final Class<T> baseType, String modid) {
        return new RegistryObject<>(name, baseType, modid);
    }

    /**
     * Factory for a {@link RegistryObject} that stores the value of an object from a registry once it is ready based on a lookup of the provided registry key.
     * <p>
     * If a registry with the given key cannot be found, an exception will be thrown when trying to fill this RegistryObject.
     * Use {@link #createOptional(ResourceLocation, ResourceKey, String)} for RegistryObjects of optional registries.
     *
     * @param name the name of the object to look up in a registry
     * @param registryKey the key of the registry. Supports lookups on {@link BuiltinRegistries}, {@link Registry}, and {@link RegistryManager#ACTIVE}.
     * @param modid the mod id calling context
     * @return a {@link RegistryObject} that stores the value of an object from a registry once it is ready
     * @see #createOptional(ResourceLocation, ResourceKey, String)
     * @see #create(ResourceLocation, IForgeRegistry)
     * @see #create(ResourceLocation, ResourceLocation, String)
     * @deprecated Use {@link #create(ResourceLocation, ResourceKey, String)} instead.
     */
    @Deprecated(forRemoval = true, since = "1.18.2")
    public static <T, U extends T> RegistryObject<U> of(final ResourceLocation name, final ResourceKey<? extends Registry<T>> registryKey, String modid)
    {
        return new RegistryObject<>(name, registryKey.location(), modid, false);
    }

    /**
     * Factory for a {@link RegistryObject} that stores the value of an object from a registry once it is ready based on a lookup of the provided registry key.
     * <p>
     * If a registry with the given key cannot be found, an exception will be thrown when trying to fill this RegistryObject.
     * Use {@link #createOptional(ResourceLocation, ResourceKey, String)} for RegistryObjects of optional registries.
     *
     * @param name the name of the object to look up in a registry
     * @param registryKey the key of the registry. Supports lookups on {@link BuiltinRegistries}, {@link Registry}, and {@link RegistryManager#ACTIVE}.
     * @param modid the mod id calling context
     * @return a {@link RegistryObject} that stores the value of an object from a registry once it is ready
     * @see #createOptional(ResourceLocation, ResourceKey, String)
     * @see #create(ResourceLocation, IForgeRegistry)
     * @see #create(ResourceLocation, ResourceLocation, String)
     */
    public static <T, U extends T> RegistryObject<U> create(final ResourceLocation name, final ResourceKey<? extends Registry<T>> registryKey, String modid)
    {
        return new RegistryObject<>(name, registryKey.location(), modid, false);
    }

    /**
     * Factory for a {@link RegistryObject} that optionally stores the value of an object from a registry once it is ready if the registry exists
     * based on a lookup of the provided registry key.
     * <p>
     * If a registry with the given key cannot be found, it will be silently ignored and this RegistryObject will not be filled.
     * Use {@link #create(ResourceLocation, ResourceKey, String)} for RegistryObjects that should throw exceptions on missing registry.
     *
     * @param name the name of the object to look up in a registry
     * @param registryKey the key of the registry. Supports lookups on {@link BuiltinRegistries}, {@link Registry}, and {@link RegistryManager#ACTIVE}.
     * @param modid the mod id calling context
     * @return a {@link RegistryObject} that stores the value of an object from a registry once it is ready
     * @see #create(ResourceLocation, ResourceKey, String)
     * @see #create(ResourceLocation, IForgeRegistry)
     * @see #create(ResourceLocation, ResourceLocation, String)
     */
    public static <T, U extends T> RegistryObject<U> createOptional(final ResourceLocation name, final ResourceKey<? extends Registry<T>> registryKey,
            String modid)
    {
        return new RegistryObject<>(name, registryKey.location(), modid, true);
    }

    /**
     * Factory for a {@link RegistryObject} that stores the value of an object from a registry once it is ready based on a lookup of the provided registry name.
     * <p>
     * If a registry with the given name cannot be found, an exception will be thrown when trying to fill this RegistryObject.
     * Use {@link #createOptional(ResourceLocation, ResourceLocation, String)} for RegistryObjects of optional registries.
     *
     * @param name the name of the object to look up in a registry
     * @param registryName the name of the registry. Supports lookups on {@link BuiltinRegistries}, {@link Registry}, and {@link RegistryManager#ACTIVE}.
     * @param modid the mod id calling context
     * @return a {@link RegistryObject} that stores the value of an object from a registry once it is ready
     * @see #createOptional(ResourceLocation, ResourceLocation, String)
     * @see #create(ResourceLocation, IForgeRegistry)
     * @see #create(ResourceLocation, ResourceKey, String)
     * @deprecated Use {@link #create(ResourceLocation, ResourceLocation, String)} instead.
     */
    @Deprecated(forRemoval = true, since = "1.18.2")
    public static <T, U extends T> RegistryObject<U> of(final ResourceLocation name, final ResourceLocation registryName, String modid)
    {
        return new RegistryObject<>(name, registryName, modid, false);
    }

    /**
     * Factory for a {@link RegistryObject} that stores the value of an object from a registry once it is ready based on a lookup of the provided registry name.
     * <p>
     * If a registry with the given name cannot be found, an exception will be thrown when trying to fill this RegistryObject.
     * Use {@link #createOptional(ResourceLocation, ResourceLocation, String)} for RegistryObjects of optional registries.
     *
     * @param name the name of the object to look up in a registry
     * @param registryName the name of the registry. Supports lookups on {@link BuiltinRegistries}, {@link Registry}, and {@link RegistryManager#ACTIVE}.
     * @param modid the mod id calling context
     * @return a {@link RegistryObject} that stores the value of an object from a registry once it is ready
     * @see #createOptional(ResourceLocation, ResourceLocation, String)
     * @see #create(ResourceLocation, IForgeRegistry)
     * @see #create(ResourceLocation, ResourceKey, String)
     */
    public static <T, U extends T> RegistryObject<U> create(final ResourceLocation name, final ResourceLocation registryName, String modid)
    {
        return new RegistryObject<>(name, registryName, modid, false);
    }

    /**
     * Factory for a {@link RegistryObject} that optionally stores the value of an object from a registry once it is ready if the registry exists
     * based on a lookup of the provided registry name.
     * <p>
     * If a registry with the given name cannot be found, it will be silently ignored and this RegistryObject will not be filled.
     * Use {@link #create(ResourceLocation, ResourceLocation, String)} for RegistryObjects that should throw exceptions on missing registry.
     *
     * @param name the name of the object to look up in a registry
     * @param registryName the name of the registry. Supports lookups on {@link BuiltinRegistries}, {@link Registry}, and {@link RegistryManager#ACTIVE}.
     * @param modid the mod id calling context
     * @return a {@link RegistryObject} that stores the value of an object from a registry once it is ready
     * @see #create(ResourceLocation, ResourceLocation, String)
     * @see #create(ResourceLocation, IForgeRegistry)
     * @see #create(ResourceLocation, ResourceKey, String)
     */
    public static <T, U extends T> RegistryObject<U> createOptional(final ResourceLocation name, final ResourceLocation registryName, String modid)
    {
        return new RegistryObject<>(name, registryName, modid, true);
    }

    private static RegistryObject<?> EMPTY = new RegistryObject<>();

    private static <T> RegistryObject<T> empty() {
        @SuppressWarnings("unchecked")
        RegistryObject<T> t = (RegistryObject<T>) EMPTY;
        return t;
    }

    private RegistryObject() {
        this.name = null;
        this.optionalRegistry = false;
    }

    @Deprecated(forRemoval = true, since = "1.18.2")
    private <V extends IForgeRegistryEntry<V>> RegistryObject(ResourceLocation name, Supplier<Class<? super V>> registryType)
    {
        this(name, RegistryManager.ACTIVE.<V>getRegistry(registryType.get()));
    }

    @SuppressWarnings("unchecked")
    private <V extends IForgeRegistryEntry<V>> RegistryObject(ResourceLocation name, IForgeRegistry<V> registry)
    {
        if (registry == null)
            throw new IllegalArgumentException("Invalid registry argument, must not be null");
        this.name = name;
        this.optionalRegistry = false;
        ObjectHolderRegistry.addHandler(pred ->
        {
            if (pred.test(registry.getRegistryName()))
                this.updateReference((IForgeRegistry<? extends T>) registry);
        });
        this.updateReference(((IForgeRegistry<? extends T>) registry));
    }

    @SuppressWarnings("unchecked")
    @Deprecated(forRemoval = true, since = "1.18.2")
    private <V extends IForgeRegistryEntry<V>> RegistryObject(final ResourceLocation name, final Class<V> baseType, final String modid)
    {
        this.name = name;
        this.optionalRegistry = false;
        final Throwable callerStack = new Throwable("Calling Site from mod: " + modid);
        ObjectHolderRegistry.addHandler(new Consumer<Predicate<ResourceLocation>>()
        {
            private IForgeRegistry<V> registry;
            private boolean invalidRegistry = false;

            @Override
            public void accept(Predicate<ResourceLocation> pred)
            {
                if (invalidRegistry)
                    return;
                if (registry == null)
                {
                    this.registry = RegistryManager.ACTIVE.getRegistry(baseType);
                    if (registry == null)
                    {
                        invalidRegistry = true;
                        throw new IllegalStateException("Unable to find registry for type " + baseType.getName() + " for mod \"" + modid + "\". Check the 'caused by' to see further stack.", callerStack);
                    }
                }
                if (pred.test(registry.getRegistryName()))
                    RegistryObject.this.updateReference((IForgeRegistry<? extends T>) registry);
            }
        });
        IForgeRegistry<V> registry = RegistryManager.ACTIVE.getRegistry(baseType);
        // allow registry to be null, this might be for a custom registry that does not exist yet
        if (registry != null)
        {
            this.updateReference(((IForgeRegistry<? extends T>) registry));
        }
    }

    private RegistryObject(final ResourceLocation name, final ResourceLocation registryName, final String modid, boolean optionalRegistry)
    {
        this.name = name;
        this.optionalRegistry = optionalRegistry;
        final Throwable callerStack = new Throwable("Calling Site from mod: " + modid);
        ObjectHolderRegistry.addHandler(new Consumer<>()
        {
            private boolean registryExists = false;
            private boolean invalidRegistry = false;

            @Override
            public void accept(Predicate<ResourceLocation> pred)
            {
                if (invalidRegistry)
                    return;
                if (!RegistryObject.this.optionalRegistry && !registryExists)
                {
                    if (!registryExists(registryName))
                    {
                        invalidRegistry = true;
                        throw new IllegalStateException("Unable to find registry with key " + registryName + " for mod \"" + modid + "\". Check the 'caused by' to see further stack.", callerStack);
                    }
                    registryExists = true;
                }
                if (pred.test(registryName))
                    RegistryObject.this.updateReference(registryName);
            }
        });
        this.updateReference(registryName);
    }

    /**
     * Directly retrieves the wrapped Registry Object. This value will automatically be updated when the backing registry is updated.
     * Will throw NPE if the value is null, use isPresent to check first. Or use any of the other guarded functions.
     */
    @Override
    @Nonnull
    public T get()
    {
        T ret = this.value;
        Objects.requireNonNull(ret, () -> "Registry Object not present: " + this.name);
        return ret;
    }

    @Deprecated(since = "1.18.1") // TODO: make package-private
    public void updateReference(IForgeRegistry<? extends T> registry)
    {
        this.value = registry.containsKey(this.name) ? registry.getValue(this.name) : null;
    }

    void updateReference(Registry<? extends T> registry)
    {
        this.value = registry.containsKey(this.name) ? registry.get(this.name) : null;
    }

    @SuppressWarnings("unchecked")
    void updateReference(ResourceLocation registryName)
    {
        IForgeRegistry<? extends T> forgeRegistry = RegistryManager.ACTIVE.getRegistry(registryName);
        if (forgeRegistry != null)
        {
            updateReference(forgeRegistry);
            return;
        }

        Registry<? extends T> vanillaRegistry = (Registry<? extends T>) Registry.REGISTRY.get(registryName);
        if (vanillaRegistry != null)
        {
            updateReference(vanillaRegistry);
            return;
        }

        Registry<? extends T> builtinRegistry = (Registry<? extends T>) BuiltinRegistries.REGISTRY.get(registryName);
        if (builtinRegistry != null)
        {
            updateReference(builtinRegistry);
            return;
        }

        this.value = null;
    }

    private static boolean registryExists(ResourceLocation registryName)
    {
        return RegistryManager.ACTIVE.getRegistry(registryName) != null
                || Registry.REGISTRY.containsKey(registryName)
                || BuiltinRegistries.REGISTRY.containsKey(registryName);
    }

    public ResourceLocation getId()
    {
        return this.name;
    }

    public Stream<T> stream() {
        return isPresent() ? Stream.of(get()) : Stream.of();
    }

    /**
     * Return {@code true} if there is a mod object present, otherwise {@code false}.
     *
     * @return {@code true} if there is a mod object present, otherwise {@code false}
     */
    public boolean isPresent() {
        return this.value != null;
    }

    /**
     * If a mod object is present, invoke the specified consumer with the object,
     * otherwise do nothing.
     *
     * @param consumer block to be executed if a mod object is present
     * @throws NullPointerException if mod object is present and {@code consumer} is
     * null
     */
    public void ifPresent(Consumer<? super T> consumer) {
        if (isPresent())
            consumer.accept(get());
    }

    /**
     * If a mod object is present, and the mod object matches the given predicate,
     * return an {@code RegistryObject} describing the value, otherwise return an
     * empty {@code RegistryObject}.
     *
     * @param predicate a predicate to apply to the mod object, if present
     * @return an {@code RegistryObject} describing the value of this {@code RegistryObject}
     * if a mod object is present and the mod object matches the given predicate,
     * otherwise an empty {@code RegistryObject}
     * @throws NullPointerException if the predicate is null
     */
    public RegistryObject<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent())
            return this;
        else
            return predicate.test(get()) ? this : empty();
    }

    /**
     * If a mod object is present, apply the provided mapping function to it,
     * and if the result is non-null, return an {@code Optional} describing the
     * result.  Otherwise return an empty {@code Optional}.
     *
     * @apiNote This method supports post-processing on optional values, without
     * the need to explicitly check for a return status.
     *
     * @param <U> The type of the result of the mapping function
     * @param mapper a mapping function to apply to the mod object, if present
     * @return an {@code Optional} describing the result of applying a mapping
     * function to the mod object of this {@code RegistryObject}, if a mod object is present,
     * otherwise an empty {@code Optional}
     * @throws NullPointerException if the mapping function is null
     */
    public<U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return Optional.empty();
        else {
            return Optional.ofNullable(mapper.apply(get()));
        }
    }

    /**
     * If a value is present, apply the provided {@code Optional}-bearing
     * mapping function to it, return that result, otherwise return an empty
     * {@code Optional}.  This method is similar to {@link #map(Function)},
     * but the provided mapper is one whose result is already an {@code Optional},
     * and if invoked, {@code flatMap} does not wrap it with an additional
     * {@code Optional}.
     *
     * @param <U> The type parameter to the {@code Optional} returned by
     * @param mapper a mapping function to apply to the mod object, if present
     *           the mapping function
     * @return the result of applying an {@code Optional}-bearing mapping
     * function to the value of this {@code Optional}, if a value is present,
     * otherwise an empty {@code Optional}
     * @throws NullPointerException if the mapping function is null or returns
     * a null result
     */
    public<U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return Optional.empty();
        else {
            return Objects.requireNonNull(mapper.apply(get()));
        }
    }

    /**
     * If a mod object is present, lazily apply the provided mapping function to it,
     * returning a supplier for the transformed result. If this object is empty, or the
     * mapping function returns {@code null}, the supplier will return {@code null}.
     *
     * @apiNote This method supports post-processing on optional values, without
     * the need to explicitly check for a return status.
     *
     * @param <U> The type of the result of the mapping function
     * @param mapper A mapping function to apply to the mod object, if present
     * @return A {@code Supplier} lazily providing the result of applying a mapping
     * function to the mod object of this {@code RegistryObject}, if a mod object is present,
     * otherwise a supplier returning {@code null}
     * @throws NullPointerException if the mapping function is {@code null}
     */
    public<U> Supplier<U> lazyMap(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return () -> isPresent() ? mapper.apply(get()) : null;
    }

    /**
     * Return the mod object if present, otherwise return {@code other}.
     *
     * @param other the mod object to be returned if there is no mod object present, may
     * be null
     * @return the mod object, if present, otherwise {@code other}
     */
    public T orElse(T other) {
        return isPresent() ? get() : other;
    }

    /**
     * Return the mod object if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
     * @param other a {@code Supplier} whose result is returned if no mod object
     * is present
     * @return the mod object if present otherwise the result of {@code other.get()}
     * @throws NullPointerException if mod object is not present and {@code other} is
     * null
     */
    public T orElseGet(Supplier<? extends T> other) {
        return isPresent() ? get() : other.get();
    }

    /**
     * Return the contained mod object, if present, otherwise throw an exception
     * to be created by the provided supplier.
     *
     * @apiNote A method reference to the exception constructor with an empty
     * argument list can be used as the supplier. For example,
     * {@code IllegalStateException::new}
     *
     * @param <X> Type of the exception to be thrown
     * @param exceptionSupplier The supplier which will return the exception to
     * be thrown
     * @return the present mod object
     * @throws X if there is no mod object present
     * @throws NullPointerException if no mod object is present and
     * {@code exceptionSupplier} is null
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return get();
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj instanceof RegistryObject) {
            return Objects.equals(((RegistryObject<?>)obj).name, name);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(name);
    }
}

/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, CloudBees, Inc., Stephen Connolly.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.cloudbees.plugins.credentials;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.DescriptorExtensionList;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.ModelObject;
import hudson.security.ACL;
import org.acegisecurity.Authentication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An extension point for providing {@link Credentials}.
 */
public abstract class CredentialsProvider implements ExtensionPoint {

    /**
     * Returns all the registered {@link com.cloudbees.plugins.credentials.Credentials} descriptors.
     *
     * @return all the registered {@link com.cloudbees.plugins.credentials.Credentials} descriptors.
     */
    public static DescriptorExtensionList<Credentials, Descriptor<Credentials>> allCredentialsDescriptors() {
        return Hudson.getInstance().getDescriptorList(Credentials.class);
    }

    /**
     * Returns the scopes allowed for credentials stored within the specified object or {@code null} if the
     * object is not relevant for scopes and the object's container should be considered instead.
     *
     * @param object the object.
     * @return the set of scopes that are relevant for the object or {@code null} if the object is not a credentials
     *         container.
     */
    public Set<CredentialsScope> getScopes(ModelObject object) {
        return null;
    }

    /**
     * Returns the credentials provided by this provider which are available to the specified {@link Authentication}
     * for items in the specified {@link ItemGroup}
     *
     * @param type           the type of credentials to return.
     * @param itemGroup      the item group (if {@code null} assume {@link hudson.model.Hudson#getInstance()}.
     * @param authentication the authentication (if {@code null} assume {@link hudson.security.ACL#SYSTEM}.
     * @return the list of credentials.
     */
    @NonNull
    public abstract <C extends Credentials> List<C> getCredentials(@NonNull Class<C> type,
                                                                   @Nullable ItemGroup itemGroup,
                                                                   @Nullable Authentication authentication);

    /**
     * Returns the credentials provided by this provider which are available to the specified {@link Authentication}
     * for the specified {@link Item}
     *
     * @param type           the type of credentials to return.
     * @param item           the item.
     * @param authentication the authentication (if {@code null} assume {@link hudson.security.ACL#SYSTEM}.
     * @return the list of credentials.
     */
    @NonNull
    public <C extends Credentials> List<C> getCredentials(@NonNull Class<C> type,
                                                          @NonNull Item item,
                                                          @Nullable Authentication authentication) {
        item.getClass();
        return getCredentials(type, item.getParent(), authentication);
    }

    /**
     * Returns all credentials which are available to the {@link ACL#SYSTEM} {@link Authentication}
     * within the {@link hudson.model.Hudson#getInstance()}.
     *
     * @param type the type of credentials to get.
     * @param <C>  the credentials type.
     * @return the list of credentials.
     */
    @NonNull
    @SuppressWarnings("unused") // API entry point for consumers
    public static <C extends Credentials> List<C> lookupCredentials(@NonNull Class<C> type) {
        return lookupCredentials(type, (Item) null, ACL.SYSTEM);
    }

    /**
     * Returns all credentials which are available to the specified {@link Authentication}
     * within the {@link hudson.model.Hudson#getInstance()}.
     *
     * @param type           the type of credentials to get.
     * @param authentication the authentication.
     * @param <C>            the credentials type.
     * @return the list of credentials.
     */
    @NonNull
    @SuppressWarnings("unused") // API entry point for consumers
    public static <C extends Credentials> List<C> lookupCredentials(@NonNull Class<C> type,
                                                                    @Nullable Authentication authentication) {
        return lookupCredentials(type, Hudson.getInstance(), authentication);
    }

    /**
     * Returns all credentials which are available to the {@link ACL#SYSTEM} {@link Authentication}
     * for use by the specified {@link Item}.
     *
     * @param type the type of credentials to get.
     * @param item the item.
     * @param <C>  the credentials type.
     * @return the list of credentials.
     */
    @NonNull
    @SuppressWarnings("unused") // API entry point for consumers
    public static <C extends Credentials> List<C> lookupCredentials(@NonNull Class<C> type,
                                                                    @Nullable Item item) {
        return item == null
                ? lookupCredentials(type, Hudson.getInstance(), ACL.SYSTEM)
                : lookupCredentials(type, item, ACL.SYSTEM);
    }

    /**
     * Returns all credentials which are available to the {@link ACL#SYSTEM} {@link Authentication}
     * for use by the {@link Item}s in the specified {@link ItemGroup}.
     *
     * @param type      the type of credentials to get.
     * @param itemGroup the item group.
     * @param <C>       the credentials type.
     * @return the list of credentials.
     */
    @NonNull
    @SuppressWarnings("unused") // API entry point for consumers
    public static <C extends Credentials> List<C> lookupCredentials(@NonNull Class<C> type,
                                                                    @Nullable ItemGroup itemGroup) {
        return lookupCredentials(type, itemGroup, ACL.SYSTEM);
    }

    /**
     * Returns all credentials which are available to the specified {@link Authentication}
     * for use by the {@link Item}s in the specified {@link ItemGroup}.
     *
     * @param type           the type of credentials to get.
     * @param itemGroup      the item group.
     * @param authentication the authentication.
     * @param <C>            the credentials type.
     * @return the list of credentials.
     */
    @NonNull
    @SuppressWarnings({"unchecked", "unused"}) // API entry point for consumers
    public static <C extends Credentials> List<C> lookupCredentials(@NonNull Class<C> type,
                                                                    @Nullable ItemGroup itemGroup,
                                                                    @Nullable Authentication
                                                                            authentication) {
        type.getClass(); // throw NPE if null
        itemGroup = itemGroup == null ? Hudson.getInstance() : itemGroup;
        authentication = authentication == null ? ACL.SYSTEM : authentication;
        ExtensionList<CredentialsProvider> providers;
        try {
            providers = Hudson.getInstance().getExtensionList(CredentialsProvider.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
        List<C> result = new ArrayList<C>();
        for (CredentialsProvider provider : providers) {
            try {
                result.addAll(provider.getCredentials(type, itemGroup, authentication));
            } catch (NoClassDefFoundError e) {
                // ignore optional dependency
            }
        }
        return result;
    }

    /**
     * Returns all credentials which are available to the specified {@link Authentication}
     * for use by the specified {@link Item}.
     *
     * @param type           the type of credentials to get.
     * @param authentication the authentication.
     * @param item           the item.
     * @param <C>            the credentials type.
     * @return the list of credentials.
     */
    @NonNull
    @SuppressWarnings("unused") // API entry point for consumers
    public static <C extends Credentials> List<C> lookupCredentials(@NonNull Class<C> type,
                                                                    @Nullable Item item,
                                                                    @Nullable Authentication
                                                                            authentication) {
        type.getClass(); // throw NPE if null
        if (item == null) {
            return lookupCredentials(type, Hudson.getInstance(), authentication);
        }
        authentication = authentication == null ? ACL.SYSTEM : authentication;
        ExtensionList<CredentialsProvider> providers;
        try {
            providers = Hudson.getInstance().getExtensionList(CredentialsProvider.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
        List<C> result = new ArrayList<C>();
        for (CredentialsProvider provider : providers) {
            try {
                result.addAll(provider.getCredentials(type, item, authentication));
            } catch (NoClassDefFoundError e) {
                // ignore optional dependency
            }
        }
        return result;
    }

    /**
     * Returns the scopes allowed for credentials stored within the specified object or {@code null} if the
     * object is not relevant for scopes and the object's container should be considered instead.
     *
     * @param object the object.
     * @return the set of scopes that are relevant for the object or {@code null} if the object is not a credentials
     *         container.
     */
    @CheckForNull
    public static Set<CredentialsScope> lookupScopes(ModelObject object) {
        ExtensionList<CredentialsProvider> providers;
        try {
            providers = Hudson.getInstance().getExtensionList(CredentialsProvider.class);
        } catch (Exception e) {
            return Collections.emptySet();
        }
        Set<CredentialsScope> result = null;
        for (CredentialsProvider provider : providers) {
            try {
                Set<CredentialsScope> scopes = provider.getScopes(object);
                if (scopes != null) {
                    // if multiple providers for the same object, then combine scopes
                    if (result == null) {
                        result = new LinkedHashSet<CredentialsScope>();
                    }
                    result.addAll(scopes);
                }
            } catch (NoClassDefFoundError e) {
                // ignore optional dependency
            }
        }
        return result;
    }
}

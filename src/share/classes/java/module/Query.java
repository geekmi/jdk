/*
 * Copyright 2006-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package java.module;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a query that determines whether or not a particular
 * module definition matches some criteria. The static methods provided return
 * query that may be used in matching <code>ModuleDefinition</code>.
 * Composition of calls can construct arbitrary nestings of constraints, as
 * the following example illustrates:</p>
 * <pre>
 * Query query = Query.and(Query.name("com.wombat.webservice"),
 *                         Query.versionRange("2.0.0+"));
 * </pre>
 *
 * <p> Unless otherwise specified, passing a <tt>null</tt> argument to any
 * method in this class will cause a {@link NullPointerException} to be thrown.
 * <p>
 * @see java.module.ModuleDefinition
 * @see java.module.VersionConstraint
 * @since 1.7
 */
public abstract class Query implements Serializable {

    private static final long serialVersionUID = 6123369458185661324L;

    /**
     * Creates a new {@code Query} instance.
     */
    protected Query() {
        // no-op
    }

    private static final Query MATCH_ALL = new AllQuery();
    private static final Query MATCH_NONE = new NoneQuery();

    /**
     * A {@code Query} object that matches everything.
     */
    public static final Query ANY = MATCH_ALL;

    /**
     * @serial include
     */
    private static class AllQuery extends Query {
        private static final long serialVersionUID = 4847340912937723526L;
        public boolean match(ModuleDefinition moduleDef)  {
            return true;
        }
        public Set<String> getIndexableNames() {
            return null;
        }
        public boolean equals(Object obj)   {
            return (obj instanceof AllQuery);
        }
        public int hashCode()   {
            return 37 * 17 + AllQuery.class.hashCode();
        }
        public String toString() {
            return "*";
        }
    };

    /**
     * @serial include
     */
    private static class NoneQuery extends Query {
        private static final long serialVersionUID = 469940504421183286L;
        public boolean match(ModuleDefinition moduleDef)  {
            return false;
        }
        public Set<String> getIndexableNames() {
            return Collections.emptySet();
        }
        public boolean equals(Object obj) {
            return (obj instanceof NoneQuery);
        }
        public int hashCode()   {
            return 37 * 17 + NoneQuery.class.hashCode();
        }
        public String toString() {
            return "NOT *";
        }
    };

    /**
     * @serial include
     */
    private static class NameQuery extends Query {
        private static final long serialVersionUID = 6249315499292409988L;
        private String name;
        NameQuery(String name) {
            this.name = name;
        }
        public Set<String> getIndexableNames() {
            Set<String> indexableNames = new HashSet<String>();
            indexableNames.add(name);
            return Collections.unmodifiableSet(indexableNames);
        }
        public boolean match(ModuleDefinition moduleDef)  {
            return moduleDef.getName().equals(name);
        }
        public boolean equals(Object obj)   {
            if (!(obj instanceof NameQuery))
                return false;
            NameQuery query = (NameQuery) obj;
            return this.name.equals(query.name);
        }
        public int hashCode()   {
            return 37 * 17 + name.hashCode();
        }
        public String toString() {
            return "name=" + name;
        }
        public String getName() {
            return name;
        }
    }

    /**
     * @serial include
     */
    private static class VersionConstraintQuery extends Query {
        private static final long serialVersionUID = -1700827011713291084L;
        private transient VersionConstraint versionConstraint;
        VersionConstraintQuery(VersionConstraint versionConstraint) {
            this.versionConstraint = versionConstraint;
        }
        public boolean match(ModuleDefinition moduleDef) {
            return versionConstraint.contains(moduleDef.getVersion());
        }
        public Set<String> getIndexableNames()  {
            return null;
        }
        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeUTF(versionConstraint.toString());
        }
        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            versionConstraint = VersionConstraint.valueOf(s.readUTF());
        }
        public boolean equals(Object obj)   {
            if (!(obj instanceof VersionConstraintQuery))
                return false;
            VersionConstraintQuery query = (VersionConstraintQuery) obj;
            return this.versionConstraint.equals(query.versionConstraint);
        }
        public int hashCode()   {
            return 37 * 17 + versionConstraint.hashCode();
        }
        public String toString() {
            return "version-constraint=" + versionConstraint;
        }
        public VersionConstraint getVersionConstraint() {
            return versionConstraint;
        }
    }

    /**
     * @serial include
     */
    private static class AttributeQuery extends Query {
        private static final long serialVersionUID = 2164892697380998474L;
        private String name;
        private String value;
        AttributeQuery(String name) {
            this.name = name;
            this.value = null;
        }
        AttributeQuery(String name, String value) {
            this.name = name;
            this.value = value;
        }
        public boolean match(ModuleDefinition moduleDef) {
            String v = moduleDef.getAttribute(name);

            // No match if attribute does not exist.
            if (v == null)
                return false;

            if (value == null)
                return true;
            else
                return (v.equals(value));
        }
        public Set<String> getIndexableNames()  {
            return null;
        }
        public boolean equals(Object obj)   {
            if (!(obj instanceof AttributeQuery))
                return false;
            AttributeQuery query = (AttributeQuery) obj;
            if (this.name.equals(query.name) == false)
                return false;
            if (this.value == null)
                return query.value == null;
            else
                return this.value.equals(query.value);
        }
        public int hashCode()   {
            int result = 17;
            result = 37 * result + name.hashCode();
            result = 37 * result + (value == null ? 0 : value.hashCode());
            return result;
        }
        public String toString() {
            if (value == null)
                return "attribute-name=" + name;
            else
                return "attribute-name=" + name + ", attribute-value=" + value;
        }
    }

    /**
     * @serial include
     */
    private static class AnnotationQuery extends Query {
        private static final long serialVersionUID = 1985739344937289141L;
        private Class annotationClass;
        AnnotationQuery(Class annotationClass)   {
            this.annotationClass = annotationClass;
        }
        @SuppressWarnings("unchecked")
        public boolean match(ModuleDefinition moduleDef)  {
            Annotation annotation = moduleDef.getAnnotation(annotationClass);
            // No match if annotation is not present.
            return (annotation != null);
        }
        public Set<String> getIndexableNames()  {
            return null;
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof AnnotationQuery))
                return false;
            AnnotationQuery query = (AnnotationQuery)obj;
            return this.annotationClass.equals(query.annotationClass);
        }
        public int hashCode() {
            int result = 17;
            result = 37 * result + annotationClass.hashCode();
            return result;
        }
        public String toString()  {
            return "annotation=" + annotationClass;
        }
    }

    /**
     * @serial include
     */
    private static class NotQuery extends Query {
        private static final long serialVersionUID = 1614304674600937513L;
        private Query query;
        NotQuery(Query query) {
            this.query = query;
        }
        public boolean match(ModuleDefinition moduleDef) {
            return !query.match(moduleDef);
        }
        public Set<String> getIndexableNames()  {
            return null;
        }
        public boolean equals(Object obj)   {
            if (!(obj instanceof NotQuery))
                return false;
            NotQuery q = (NotQuery) obj;
            return this.query.equals(q.query);
        }
        public int hashCode()   {
            return 37 * 17 + query.hashCode();
        }
        public String toString() {
            return "(NOT " + query.toString() + ")";
        }
        public Query getNegatedQuery() {
            return query;
        }
    }

    /**
     * @serial include
     */
    private static class AndQuery extends Query {
        private static final long serialVersionUID = 4220019642283496320L;
        private Query query1;
        private Query query2;
        AndQuery(Query query1, Query query2) {
            this.query1 = query1;
            this.query2 = query2;
        }
        public boolean match(ModuleDefinition moduleDef) {
            return query1.match(moduleDef) && query2.match(moduleDef);
        }
        public Set<String> getIndexableNames()  {
            Set<String> indexableNames1 = query1.getIndexableNames();
            Set<String> indexableNames2 = query2.getIndexableNames();
            if (indexableNames1 == null) {
                return indexableNames2;
            } else if (indexableNames2 == null) {
                return indexableNames1;
            } else {
                Set<String> result = new HashSet<String>(indexableNames1);
                result.retainAll(indexableNames2);
                return Collections.unmodifiableSet(result);
            }
        }
        public boolean equals(Object obj)   {
            if (!(obj instanceof AndQuery))
                return false;
            AndQuery q = (AndQuery) obj;
            return (this.query1.equals(q.query1) && this.query2.equals(q.query2))
                    || (this.query1.equals(q.query2) && this.query2.equals(q.query1));
        }
        public int hashCode()   {
            // Query.and(query1, query2).hashCode() == Query.and(query2, query1).hashCode()
            return 37 * 17 + query1.hashCode() + query2.hashCode();
        }
        public String toString() {
            return "(" + query1.toString() + " AND " + query2.toString() + ")";
        }
        public Query getLeftQuery() {
            return query1;
        }
        public Query getRightQuery() {
            return query2;
        }
    }

    /**
     * @serial include
     */
    private static class OrQuery extends Query {
        private static final long serialVersionUID = -6857000009881502154L;
        private Query query1;
        private Query query2;
        OrQuery(Query query1, Query query2) {
            this.query1 = query1;
            this.query2 = query2;
        }
        public boolean match(ModuleDefinition moduleDef) {
            return query1.match(moduleDef) || query2.match(moduleDef);
        }
        public Set<String> getIndexableNames()  {
            Set<String> indexableNames1 = query1.getIndexableNames();
            Set<String> indexableNames2 = query2.getIndexableNames();
            if (indexableNames1 == null)  {
                return indexableNames2;
            } else if (indexableNames2 == null)  {
                return indexableNames1;
            } else {
                Set<String> result = new HashSet<String>(indexableNames1);
                result.addAll(indexableNames2);
                return Collections.unmodifiableSet(result);
            }
        }
        public boolean equals(Object obj)   {
            if (!(obj instanceof OrQuery))
                return false;
            OrQuery q = (OrQuery) obj;
            return (this.query1.equals(q.query1) && this.query2.equals(q.query2))
                || (this.query1.equals(q.query2) && this.query2.equals(q.query1));
        }
        public int hashCode()   {
            // Query.or(query1, query2).hashCode() == Query.or(query2, query1).hashCode()
            return 37 * 17 + query1.hashCode() + query2.hashCode();
        }
        public String toString() {
            return "(" + query1.toString() + " OR " + query2.toString() + ")";
        }
        public Query getLeftQuery() {
            return query1;
        }
        public Query getRightQuery() {
            return query2;
        }
    }

    /**
     * Returns a {@code Query} that inverts the specified query.
     *
     * @param query the specified query.
     * @return the <code>Query</code> object.
     */
    public static Query not(Query query) {
        if (query == null)
            throw new NullPointerException("query must not be null.");

        if (query == MATCH_ALL)
            return MATCH_NONE;
        else
            return new NotQuery(query);
    }

    /**
     * Returns a {@code Query} that is the conjunction of two other queries.
     *
     * @param query1 A query.
     * @param query2 Another query.
     * @return the <code>Query</code> object.
     */
    public static Query and(Query query1, Query query2)  {
        if (query1 == null)
            throw new NullPointerException("query1 must not be null.");
        if (query2 == null)
            throw new NullPointerException("query2 must not be null.");

        // Optimize query if possible
        // ----
        if (query1 == MATCH_ALL)
            return query2;

        if (query2 == MATCH_ALL)
            return query1;

        if (query1 == MATCH_NONE || query2 == MATCH_NONE)
            return MATCH_NONE;
        // ----

        return new AndQuery(query1, query2);
    }

    /**
     * Returns a {@code Query} that is the disjunction of two other queries.
     *
     * @param query1 A query.
     * @param query2 Another query.
     * @return the <code>Query</code> object.
     */
    public static Query or(Query query1, Query query2) {
        if (query1 == null)
            throw new NullPointerException("query1 must not be null.");
        if (query2 == null)
            throw new NullPointerException("query2 must not be null.");

        // Optimize query if possible
        // ----
        if (query1 == MATCH_ALL || query2 == MATCH_ALL)
            return MATCH_ALL;

        if (query1 == MATCH_NONE)
            return query2;

        if (query2 == MATCH_NONE)
            return query1;
        // ----

        return new OrQuery(query1, query2);
    }

    /**
     * Returns a {@code Query} that requires the version of a module definition
     * to be contained within any of the ranges known to the specified
     * version constraint. The string must not contain any leading or trailing
     * whitespace.
     *
     * @param source the string to be parsed.
     * @return the <code>Query</code> object.
     * @throws IllegalArgumentException if the string does not follow
     *         the version constraint format.
     */
    public static Query version(String source) {
        if (source == null)
            throw new NullPointerException("source must not be null.");

        return version(VersionConstraint.valueOf(source));
    }

    /**
     * Returns a {@code Query} that requires the version of a module definition
     * to be contained within any of the ranges known to the specified
     * version constraint.
     *
     * @param versionConstraint the <code>VersionConstraint</code> object.
     * @return the <code>Query</code> object.
     */
    public static Query version(VersionConstraint versionConstraint) {
        if (versionConstraint == null)
            throw new NullPointerException("version constraint must not be null.");

        if (versionConstraint.equals(VersionConstraint.DEFAULT))
            return MATCH_ALL;
        else
            return new VersionConstraintQuery(versionConstraint);
    }

    /**
     * Returns a {@code Query} that requires the name of a module definition equals
     * to the specified name.
     *
     * @param name the name of the module definition.
     * @return the <code>Query</code> object.
     */
    public static Query name(String name) {
        if (name == null)
            throw new NullPointerException("name must not be null.");

        return new NameQuery(name);
    }

    /**
     * Returns a {@code Query} that requires the specified name of a module attribute
     * exists.
     *
     * @param name the name of the module attribute.
     * @return the <code>Query</code> object.
     */
    public static Query attribute(String name)  {
        if (name == null)
            throw new NullPointerException("attribute's name must not be null.");

        return new AttributeQuery(name);
    }

    /**
     * Returns a {@code Query} that requires an attribute of a module definition
     * matches the specified name and value.
     *
     * @param name  the name of the module attribute.
     * @param value the value of the module attribute.
     * @return the <code>Query</code> object.
     */
    public static Query attribute(String name, String value) {
        if (name == null)
            throw new NullPointerException("attribute's name must not be null.");
        if (value == null)
            throw new NullPointerException("attribute's value must not be null.");

        return new AttributeQuery(name, value);
    }

    /**
     * Returns a {@code Query} that requires a module definition to have annotation
     * for the specified type.
     *
     * @param annotationClass the Class object corresponding to the annotation type.
     * @return the <code>Query</code> object.
     */
    public static Query annotation(Class annotationClass) {
        if (annotationClass == null)
            throw new NullPointerException("annotation class must not be null.");

        return new AnnotationQuery(annotationClass);
    }

    /**
     * Determine if the specified module definition matches this query.
     *
     * @param target the <code>ModuleDefinition</code> to be matched.
     * @return true if the <code>ModuleDefinition</code> matches this
     *         query.
     */
    public abstract boolean match(ModuleDefinition target);

    /**
     * Returns an unmodifiable set of the indexable names of the module
     * definitions that is represented by this query.
     *
     * This method is intended to be used by the repository implementation as
     * an optimization to determine a set of module definitions that matches
     * this query solely based on the requirement on the module names.
     *
     * @return an unmodifiable set of indexable module names if it exists;
     *         returns null otherwise. If the set is empty, no module
     *         definition would match this query.
     */
    public abstract Set<String> getIndexableNames();
}

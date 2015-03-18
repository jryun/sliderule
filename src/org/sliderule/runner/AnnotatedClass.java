/*
 * Copyright (C) 2015 Christopher Friedt <chrisfriedt@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sliderule.runner;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public class AnnotatedClass {
	private static final int FIELD_PARAM;

	private static final int METHOD_AFTER_EXPERIMENT;
	private static final int METHOD_BEFORE_EXPERIMENT;
	private static final int METHOD_BENCHMARK;
	private static final int METHOD_AFTER_REP;
	private static final int METHOD_BEFORE_REP;
	private static final int METHOD_MACROBENCHMARK;

	private static final Map<Class<? extends Annotation>,Integer> field_map;
	private static final Map<Class<? extends Annotation>,Integer> method_map;

	static {
		int field=0;
		FIELD_PARAM = field++;

		field_map = new HashMap<Class<? extends Annotation>,Integer>();
		field_map.put( org.sliderule.Param.class, FIELD_PARAM );

		int method=0;
		METHOD_AFTER_EXPERIMENT = method++;
		METHOD_BEFORE_EXPERIMENT = method++;
		METHOD_BENCHMARK = method++;
		METHOD_AFTER_REP = method++;
		METHOD_BEFORE_REP = method++;
		METHOD_MACROBENCHMARK = method++;

		method_map = new HashMap<Class<? extends Annotation>,Integer>();
		method_map.put( org.sliderule.AfterExperiment.class, METHOD_AFTER_EXPERIMENT );
		method_map.put( org.sliderule.BeforeExperiment.class, METHOD_BEFORE_EXPERIMENT );
		method_map.put( org.sliderule.Benchmark.class, METHOD_BENCHMARK );
		method_map.put( org.sliderule.api.AfterRep.class, METHOD_AFTER_REP );
		method_map.put( org.sliderule.api.BeforeRep.class, METHOD_BEFORE_REP );
		method_map.put( org.sliderule.api.Macrobenchmark.class, METHOD_MACROBENCHMARK );
	}

	private final Class<?> klass;
	private final HashSet<Field>[] field_array;
	private final HashSet<Method>[] method_array;

	@SuppressWarnings("unchecked")
	public AnnotatedClass( Class<?> klass ) {
		this.klass = klass;
		field_array = new HashSet[ field_map.size() ];
		for( int i=0; i < field_array.length; i++ ) {
			field_array[i] = new HashSet<Field>();
		}
		method_array = new HashSet[ method_map.size() ];
		for( int i=0; i < method_array.length; i++ ) {
			method_array[i] = new HashSet<Method>();
		}
		
		ArrayList<Field> fs = new ArrayList<Field>();
		fs.addAll( Arrays.asList( klass.getFields() ) );
		fs.addAll( Arrays.asList( klass.getDeclaredFields() ) );

		for( Field f: fs ) {
			f.setAccessible( true );
			filterField( f );
		}

		ArrayList<Method> ms = new ArrayList<Method>();
		ms.addAll( Arrays.asList( klass.getMethods() ) );
		ms.addAll( Arrays.asList( klass.getDeclaredMethods() ) );

		for( Method m: ms ) {
			m.setAccessible( true );
			filterMethod( m );
		}
	}

	public Class<?> getAnnotatedClass() {
		return klass;
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> void filterMember( Object o ) {
		List<T> anna;
		boolean is_field;
		if ( o instanceof Field ) {
			is_field = true;
		} else if ( o instanceof Method ) {
			is_field = false;
		} else {
			throw new IllegalArgumentException();
		}
		Map<Class<? extends Annotation>,Integer> map = is_field ? field_map : method_map;
		for( Map.Entry<Class<? extends Annotation>,Integer> e: map.entrySet() ) {
			Class<? extends Annotation> klass = e.getKey();
			int val = (int)(Integer)e.getValue();
			anna = new ArrayList<T>();
			anna.addAll( (Collection<? extends T>) Arrays.asList( getAnnotationsByType( o, klass ) ) );
			anna.addAll( (Collection<? extends T>) Arrays.asList( getDeclaredAnnotationsByType( o, klass ) ) );
			if ( ! anna.isEmpty() ) {
				if ( is_field ) {
					field_array[ val ].add( (Field) o );
				} else {
					method_array[ val ].add( (Method) o );
				}
			}
		}
	}

	private <T extends Annotation> void filterField( Field f ) {
		filterMember( f );
	}

	private <T extends Annotation> void filterMethod( Method m ) {
		filterMember( m );
	}


	@SuppressWarnings("unchecked")
	private static <T extends Annotation> T[] getAnnotations( boolean declared, Object o, Class<T> klass ) {
		T[] a = null;
		if ( o instanceof Field ) {
			Field f = (Field)o;
			a = (T[]) ( declared ? f.getDeclaredAnnotations() : f.getAnnotations() );
		} else if ( o instanceof Method ) {
			Method m = (Method)o;
			a = (T[]) ( declared ? m.getDeclaredAnnotations() : m.getAnnotations() );
		} else {
			throw new IllegalArgumentException();
		}

		ArrayList<T> ala = new ArrayList<T>();
		for( T an: a ) {
			Class<? extends Annotation> ant = an.annotationType();
			if ( ant == klass ) {
				ala.add( (T) an );
			}
		}
		T[] r = ala.toArray( a );
		if ( 1 == r.length && null == r[0] ) {
			r = (T[]) Array.newInstance( klass, 0 );
		}
		return r;
	}
	static <T extends Annotation> T[] getAnnotationsByType( Object o, Class<T> klass ) {
		return getAnnotations( false, o, klass );
	}
	static <T extends Annotation> T[] getDeclaredAnnotationsByType( Object o, Class<T> klass ) {
		return getAnnotations( true, o, klass );
	}

	public Set<Field> getParamFields() {
		return field_array[ FIELD_PARAM ];
	}

	public Set<Method> getAfterExperimentMethods() {
		return method_array[ METHOD_AFTER_EXPERIMENT ];
	}
	public Set<Method> getBeforeExperimentMethods() {
		return method_array[ METHOD_BEFORE_EXPERIMENT ];
	}
	public Set<Method> getBenchmarkMethods() {
		return method_array[ METHOD_BENCHMARK ];
	}
	public Set<Method> getAfterRepMethods() {
		return method_array[ METHOD_AFTER_REP ];
	}
	public Set<Method> getBeforeRepMethods() {
		return method_array[ METHOD_BEFORE_REP ];
	}
	public Set<Method> getMacrobenchmarkMethods() {
		return method_array[ METHOD_MACROBENCHMARK ];
	}
}

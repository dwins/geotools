/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2014, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.data.solr;

import javax.measure.unit.SI;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

public class SolrGeometryTest extends SolrTestSupport {

    public void testCrossesFilter() throws Exception {
        init("not-active");
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        GeometryFactory gf = new GeometryFactory();
        PackedCoordinateSequenceFactory sf = new PackedCoordinateSequenceFactory();
        LineString ls = gf.createLineString(sf.create(new double[] { 0, 0, 2, 2 }, 2));
        Crosses f = ff.crosses(ff.property("geo"), ff.literal(ls));
        SimpleFeatureCollection features = featureSource.getFeatures(f);
        assertEquals(1, features.size());
        SimpleFeatureIterator fsi = features.features();
        assertTrue(fsi.hasNext());
        assertEquals(fsi.next().getID(), "not-active.12");
    }

    public void testNotCrossesFilter() throws Exception {
        init("not-active");
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        GeometryFactory gf = new GeometryFactory();
        PackedCoordinateSequenceFactory sf = new PackedCoordinateSequenceFactory();
        LineString ls = gf.createLineString(sf.create(new double[] { 0, 0, 1, 1 }, 2));
        Crosses f = ff.crosses(ff.property("geo"), ff.literal(ls));
        SimpleFeatureCollection features = featureSource.getFeatures(f);
        assertEquals(0, features.size());
    }

    public void testEqualFilter() throws Exception {
        init("not-active");
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        GeometryFactory gf = new GeometryFactory();
        PackedCoordinateSequenceFactory sf = new PackedCoordinateSequenceFactory();
        Polygon ls = gf.createPolygon(sf.create(new double[] { 3, 2, 6, 2, 6, 7, 3, 7, 3, 2 }, 2));
        Equals f = ff.equal(ff.property("geo"), ff.literal(ls));
        SimpleFeatureCollection features = featureSource.getFeatures(f);
        assertEquals(1, features.size());
        SimpleFeatureIterator fsi = features.features();
        assertTrue(fsi.hasNext());
        assertEquals(fsi.next().getID(), "not-active.13");
    }

    public void testDisjointFilter() throws Exception {
        init("not-active");
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        GeometryFactory gf = new GeometryFactory();
        PackedCoordinateSequenceFactory sf = new PackedCoordinateSequenceFactory();
        Point ls = gf.createPoint(sf.create(new double[] { 0, 0 }, 2));
        Disjoint f = ff.disjoint(ff.property("geo"), ff.literal(ls));
        SimpleFeatureCollection features = featureSource.getFeatures(f);
        assertEquals(2, features.size());
        SimpleFeatureIterator fsi = features.features();
        assertTrue(fsi.hasNext());
        assertEquals(fsi.next().getID(), "not-active.12");
        assertTrue(fsi.hasNext());
        assertEquals(fsi.next().getID(), "not-active.13");
    }

    public void testTouchesFilter() throws Exception {
        init("not-active");
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        GeometryFactory gf = new GeometryFactory();
        PackedCoordinateSequenceFactory sf = new PackedCoordinateSequenceFactory();
        Point ls = gf.createPoint(sf.create(new double[] { 1, 1 }, 2));
        Touches f = ff.touches(ff.property("geo"), ff.literal(ls));
        SimpleFeatureCollection features = featureSource.getFeatures(f);
        assertEquals(1, features.size());
        SimpleFeatureIterator fsi = features.features();
        assertTrue(fsi.hasNext());
        assertEquals(fsi.next().getID(), "not-active.12");
    }

    public void testWithinFilter() throws Exception {
        init("not-active");
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        GeometryFactory gf = new GeometryFactory();
        PackedCoordinateSequenceFactory sf = new PackedCoordinateSequenceFactory();
        Polygon ls = gf.createPolygon(sf.create(new double[] { 0, 0, 0, 6, 6, 6, 6, 0, 0, 0 }, 2));
        Within f = ff.within(ff.property("geo"), ff.literal(ls));
        SimpleFeatureCollection features = featureSource.getFeatures(f);
        assertEquals(1, features.size());
        SimpleFeatureIterator fsi = features.features();
        assertTrue(fsi.hasNext());
        assertEquals(fsi.next().getID(), "not-active.12");
    }

    public void testOverlapsFilter() throws Exception {
        init("not-active");
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        GeometryFactory gf = new GeometryFactory();
        PackedCoordinateSequenceFactory sf = new PackedCoordinateSequenceFactory();
        Polygon ls = gf.createPolygon(sf.create(
                new double[] { 5.5, 6, 7, 6, 7, 7, 5.5, 7, 5.5, 6 }, 2));
        Overlaps f = ff.overlaps(ff.property("geo"), ff.literal(ls));
        SimpleFeatureCollection features = featureSource.getFeatures(f);
        assertEquals(1, features.size());
        SimpleFeatureIterator fsi = features.features();
        assertTrue(fsi.hasNext());
        assertEquals(fsi.next().getID(), "not-active.13");
    }

    public void testIntersectsFilter() throws Exception {
        init("not-active");
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        GeometryFactory gf = new GeometryFactory();
        PackedCoordinateSequenceFactory sf = new PackedCoordinateSequenceFactory();
        Polygon ls = gf.createPolygon(sf.create(new double[] { 6, 6, 7, 6, 7, 7, 6, 7, 6, 6 }, 2));
        Intersects f = ff.intersects(ff.property("geo"), ff.literal(ls));
        SimpleFeatureCollection features = featureSource.getFeatures(f);
        assertEquals(1, features.size());
        SimpleFeatureIterator fsi = features.features();
        assertTrue(fsi.hasNext());
        assertEquals(fsi.next().getID(), "not-active.13");
    }

    public void testContainsFilter() throws Exception {
        init("not-active");
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        GeometryFactory gf = new GeometryFactory();
        PackedCoordinateSequenceFactory sf = new PackedCoordinateSequenceFactory();
        Polygon ls = gf.createPolygon(sf.create(new double[] { 2, 2, 3, 2, 3, 3, 2, 3, 2, 2 }, 2));
        Contains f = ff.contains(ff.property("geo"), ff.literal(ls));
        SimpleFeatureCollection features = featureSource.getFeatures(f);
        assertEquals(1, features.size());
        SimpleFeatureIterator fsi = features.features();
        assertTrue(fsi.hasNext());
        assertEquals(fsi.next().getID(), "not-active.12");
    }

    public void testDWithinFilter() throws Exception {
        init("not-active");
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        GeometryFactory gf = new GeometryFactory();
        PackedCoordinateSequenceFactory sf = new PackedCoordinateSequenceFactory();
        Point ls = gf.createPoint(sf.create(new double[] { 1, 1 }, 2));
        DWithin f = ff.dwithin(ff.property("geo"), ff.literal(ls), 3, SI.METRE.getSymbol());
        SimpleFeatureCollection features = featureSource.getFeatures(f);
        assertEquals(2, features.size());
        SimpleFeatureIterator fsi = features.features();
        assertTrue(fsi.hasNext());
        assertEquals(fsi.next().getID(), "not-active.12");
        assertTrue(fsi.hasNext());
        assertEquals(fsi.next().getID(), "not-active.13");
    }

    public void testBeyondFilter() throws Exception {
        init("not-active");
        FilterFactory2 ff = (FilterFactory2) dataStore.getFilterFactory();
        GeometryFactory gf = new GeometryFactory();
        PackedCoordinateSequenceFactory sf = new PackedCoordinateSequenceFactory();
        Point ls = gf.createPoint(sf.create(new double[] { 1, 1 }, 2));
        Beyond f = ff.beyond(ff.property("geo"), ff.literal(ls), 1, SI.METRE.getSymbol());
        SimpleFeatureCollection features = featureSource.getFeatures(f);
        assertEquals(1, features.size());
        SimpleFeatureIterator fsi = features.features();
        assertTrue(fsi.hasNext());
        assertEquals(fsi.next().getID(), "not-active.13");
    }

}

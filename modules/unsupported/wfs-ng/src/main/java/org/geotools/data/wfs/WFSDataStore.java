/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008-2014, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.wfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.wfs.internal.DescribeFeatureTypeRequest;
import org.geotools.data.wfs.internal.DescribeFeatureTypeResponse;
import org.geotools.data.wfs.internal.WFSClient;
import org.geotools.data.wfs.internal.parsers.EmfAppSchemaParser;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.NameImpl;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

public class WFSDataStore extends ContentDataStore {

    private final WFSClient client;

    private final Map<Name, QName> names;

    private final Map<QName, FeatureType> remoteFeatureTypes;

    public WFSDataStore(final WFSClient client) {
        this.client = client;
        this.names = new ConcurrentHashMap<Name, QName>();
        this.remoteFeatureTypes = new ConcurrentHashMap<QName, FeatureType>();

        // default factories
        setFilterFactory(CommonFactoryFinder.getFilterFactory(null));
        setGeometryFactory(new GeometryFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY));
        setFeatureTypeFactory(new FeatureTypeFactoryImpl());
        setFeatureFactory(CommonFactoryFinder.getFeatureFactory(null));
    }

    /**
     * @see WFSDataStore#getInfo()
     */
    @Override
    public WFSServiceInfo getInfo() {
        return client.getInfo();
    }

    @Override
    protected WFSContentState createContentState(ContentEntry entry) {
        return new WFSContentState(entry);
    }

    /**
     * @see org.geotools.data.store.ContentDataStore#createTypeNames()
     */
    @Override
    protected List<Name> createTypeNames() throws IOException {
        String namespaceURI = getNamespaceURI();

        Set<QName> remoteTypeNames = client.getRemoteTypeNames();
        List<Name> names = new ArrayList<Name>(remoteTypeNames.size());
        for (QName remoteTypeName : remoteTypeNames) {
            String localTypeName = remoteTypeName.getLocalPart();
            if (!XMLConstants.DEFAULT_NS_PREFIX.equals(remoteTypeName.getPrefix())) {
                localTypeName = remoteTypeName.getPrefix() + "_" + localTypeName;
            }
            Name typeName = new NameImpl(namespaceURI==null? remoteTypeName.getNamespaceURI() : namespaceURI, localTypeName);
            
            names.add(typeName);
            this.names.put(typeName, remoteTypeName);
        }
        return names;
    }

    /**
     * @see WFSFeatureSource
     * @see WFSFeatureStore
     * @see WFSClient#supportsTransaction(QName)
     * @see org.geotools.data.store.ContentDataStore#createFeatureSource(org.geotools.data.store.ContentEntry)
     */
    @Override
    protected ContentFeatureSource createFeatureSource(final ContentEntry entry) throws IOException {
        ContentFeatureSource source;

        source = new WFSFeatureSource(entry, client);

        final QName remoteTypeName = getRemoteTypeName(entry.getName());
        
        if (client.supportsTransaction(remoteTypeName)) {
         source = new WFSFeatureStore((WFSFeatureSource) source);
        }

        return source;
    }

    public QName getRemoteTypeName(Name localTypeName) throws IOException {
        if (names.isEmpty()) {
            createTypeNames();
        }
        QName qName = names.get(localTypeName);
        if (null == qName) {
            throw new NoSuchElementException(localTypeName.toString());
        }
        return qName;
    }

    public FeatureType getRemoteFeatureType(final QName remoteTypeName) throws IOException {

        FeatureType remoteFeatureType;

        final String lockObj = remoteTypeName.toString().intern();

        synchronized (lockObj) {
            remoteFeatureType = remoteFeatureTypes.get(remoteTypeName);
            if (remoteFeatureType == null) {

                DescribeFeatureTypeRequest request = client.createDescribeFeatureTypeRequest();
                request.setTypeName(remoteTypeName);

                DescribeFeatureTypeResponse response = client.issueRequest(request);

                remoteFeatureType = response.getFeatureType();
                remoteFeatureTypes.put(remoteTypeName, remoteFeatureType);
            }
        }

        return remoteFeatureType;
    }

    public SimpleFeatureType getRemoteSimpleFeatureType(final QName remoteTypeName)
            throws IOException {

        final FeatureType remoteFeatureType = getRemoteFeatureType(remoteTypeName);
        final SimpleFeatureType remoteSimpleFeatureType;
        // remove GML properties
        remoteSimpleFeatureType = EmfAppSchemaParser.toSimpleFeatureType(remoteFeatureType);

        return remoteSimpleFeatureType;
    }

    public WFSClient getWfsClient() {
        return client;
    }

}

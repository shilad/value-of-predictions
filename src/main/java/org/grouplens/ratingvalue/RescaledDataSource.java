package org.grouplens.ratingvalue;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.data.DataSource;

/**
 * Created with IntelliJ IDEA.
 * User: shilad
 * Date: 4/2/12
 * Time: 9:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class RescaledDataSource implements DataSource {

    private DataSource delegate;
    private PreferenceDomainMapper mapper;

    public RescaledDataSource(PreferenceDomainMapper mapper, DataSource dao) {
        this.delegate = dao;
        this.mapper = mapper;
    }

    @Override
    public String getName() {
        return "rescaled " + delegate.getName();
    }

    @Override
    public PreferenceDomain getPreferenceDomain() {
        return mapper.getOutDomain();
    }

    @Override
    public DAOFactory getDAOFactory() {
        return new RescaledRatingDao.Factory(mapper, delegate.getDAOFactory());
    }

    @Override
    public long lastModified() {
        return delegate.lastModified();
    }
}

package org.grouplens.ratingvalue;

import com.google.common.base.Function;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;

import javax.annotation.Nullable;

public class UserFilteringDataSourceWrapper implements Function<DAOFactory,DAOFactory> {
    @Override
    public DAOFactory apply(@Nullable DAOFactory daoFactory) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    static class FilteringDAOFactory implements DAOFactory {
        @Override
        public DataAccessObject create() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public DataAccessObject snapshot() {
            return create();
        }
    }
}

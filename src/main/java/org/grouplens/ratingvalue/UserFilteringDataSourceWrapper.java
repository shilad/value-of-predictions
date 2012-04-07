package org.grouplens.ratingvalue;

import com.google.common.base.Function;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class UserFilteringDataSourceWrapper implements Function<DAOFactory,DAOFactory> {
    private double sampleFraction;

    public UserFilteringDataSourceWrapper(double sampleFraction) {
        this.sampleFraction = sampleFraction;
    }

    @Override
    public DAOFactory apply(@Nullable DAOFactory daoFactory) {
        return new FilteringDAOFactory(daoFactory);
    }

    class FilteringDAOFactory implements DAOFactory {
        private DAOFactory delegate;

        FilteringDAOFactory(DAOFactory delegate) {
            this.delegate = delegate;
        }
        @Override
        public DataAccessObject create() {
            List<Event> sampled = new ArrayList<Event>();
            DataAccessObject dao = delegate.create();
            for (Event event : dao.getEvents()) {
                if (event instanceof Rating && Math.random() > sampleFraction) {
                    continue;
                }
                sampled.add(event);
            }
            return new EventCollectionDAO(sampled);
        }

        @Override
        public DataAccessObject snapshot() {
            return create();
        }
    }
}

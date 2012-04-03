package org.grouplens.ratingvalue;

import org.grouplens.lenskit.cursors.AbstractCursor;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.cursors.LongCursor;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.*;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.norm.PreferenceDomainQuantizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.WillCloseWhenClosed;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class RescaledRatingDao implements DataAccessObject {
    protected static final Logger logger = LoggerFactory.getLogger(RescaledRatingDao.class);


    private final EventCollectionDAO delegate;
    private final DataAccessObject dao;
    private double[] thresholds;
    private final PreferenceDomainMapper mapper;


    public RescaledRatingDao(PreferenceDomainMapper mapper, DataAccessObject dao) {
        this.mapper = mapper;
        List<Event> transformed = new ArrayList<Event>();
        for (Event event : dao.getEvents()) {
            if (event instanceof Rating) {
                event = rescale((Rating) event);
            }
            transformed.add(event);
        }
        this.delegate = new EventCollectionDAO(transformed);
        this.dao = dao;
    }
    
    public Rating rescale(Rating rating) {
        if (rating.getPreference() == null) {
            return rating;
        }
        double r1 = rating.getPreference().getValue();
        double r2 = mapper.map(r1);
        return new SimpleRating(rating.getId(), rating.getUserId(), rating.getItemId(), r2, rating.getTimestamp());
    }

    public DataAccessObject getDao() {
        return this.dao;
    }

    @Override
    public LongCursor getUsers() {
        return delegate.getUsers();
    }

    @Override
    public <E extends Event> Cursor<E> getUserEvents(long user, Class<E> type) {
        return delegate.getUserEvents(user, type);
    }

    @Override
    public Cursor<UserHistory<Event>> getUserHistories() {
        return delegate.getUserHistories();
    }

    @Override
    public <E extends Event> Cursor<UserHistory<E>> getUserHistories(Class<E> type) {
        return delegate.getUserHistories(type);
    }

    @Override
    public <E extends Event> Cursor<E> getItemEvents(long item, Class<E> type) {
        return delegate.getItemEvents(item, type);
    }

    @Override
    public Cursor<Event> getEvents() {
        return delegate.getEvents();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public Cursor<? extends Event> getEvents(SortOrder order) {
        return delegate.getEvents(order);
    }

    @Override
    public <E extends Event> Cursor<E> getEvents(Class<E> type) {
        return delegate.getEvents(type);
    }

    @Override
    public <E extends Event> Cursor<E> getEvents(Class<E> type, SortOrder order) {
        return delegate.getEvents(type, order);
    }

    @Override
    public Cursor<? extends Event> getUserEvents(long userId) {
        return delegate.getUserEvents(userId);
    }

    @Override
    public UserHistory<Event> getUserHistory(long user) {
        return delegate.getUserHistory(user);
    }

    @Override
    public <E extends Event> UserHistory<E> getUserHistory(long user, Class<E> type) {
        return delegate.getUserHistory(user, type);
    }

    @Override
    public Cursor<? extends Event> getItemEvents(long itemId) {
        return delegate.getItemEvents(itemId);
    }

    @Override
    public LongCursor getItems() {
        return delegate.getItems();
    }

    @Override
    public int getItemCount() {
        return delegate.getItemCount();
    }

    @Override
    public int getUserCount() {
        return delegate.getUserCount();
    }
    
    public static class Factory implements DAOFactory {
        private final DAOFactory daoFactory;
        private double[] thresholds;
        private PreferenceDomainMapper mapper;

        public Factory(PreferenceDomainMapper mapper, DAOFactory daoFactory) {
            this.mapper = mapper;
            this.daoFactory = daoFactory;
        }

        @Override
        public DataAccessObject create() {
            return new RescaledRatingDao(mapper, daoFactory.create());
        }

        @Override
        public DataAccessObject snapshot() {
            return create();
        }
    }
}

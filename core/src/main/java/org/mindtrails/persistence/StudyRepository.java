package org.mindtrails.persistence;

import org.mindtrails.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;


import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dan
 * Date: 3/19/14
 * Time: 4:42 PM
 * This causes Spring to automatically create CRUD operations for the
 * participant object:
 *    delete(T entity) which deletes the entity given as a parameter.
 *    findAll() which returns a list of entities.
 *    findOne(ID id) which returns the entity using the id given a parameter as a search criteria.
 *    save(T entity) which saves the entity given as a parameter.
 * Additional methods will be provided automatically by following a standard
 * naming convention, as is the case with findByEmailAddress
 */

public interface StudyRepository extends JpaRepository<BaseStudy, Long> {



    List<StudyStats> findAllStatsBy();

    List<Study> findAllByConditioningEquals(String condition);

    Study findById(long id);

}


package main.repository;

import main.model.Index;

import java.util.List;

public interface IndexRepositoryCustom {
    void insertIndexList(String siteName, List<Index> indices);
}

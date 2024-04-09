function random_delete_bins(rec, num_bins_to_delete)
    if not aerospike:exists(rec) then
        info("Record does not exist")
        return nil
    end
    local bins = record.bin_names(rec)
    local bin_count = #bins
    if bin_count == 0 then
        info("No bins found in record")
        return nil
    end
    info("Record does 1")
    num_bins_to_delete = math.min(num_bins_to_delete, bin_count)
    info("Deleting " .. num_bins_to_delete .. " bins randomly")
    info("Record does 2")
    local bins_to_delete = {}
    for i = 1, num_bins_to_delete do
        local random_bin_index = math.random(#bins)
        local bin_to_delete = table.remove(bins, random_bin_index)
        info("Record does "..bin_to_delete)
        rec[bin_to_delete] = nil
        table.insert(bins_to_delete, bin_to_delete)
        info("Record does "..i.." end")
    end
    aerospike:update(rec)
    local str = table.concat(bins_to_delete, ", ")
    info("Deleted bins: " .. str)
    return str
end

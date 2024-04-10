function getBinNames(rec, count)
    if not aerospike:exists(rec) then
        return nil
    end
    local binNames = record.bin_names(rec)
    local result = {}
    for i = 1, count do
        if binNames[i] then
            table.insert(result, binNames[i])
        else
            break
        end
    end
    return table.concat(result, ",")
end